package kaamverse.bl.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import kaamverse.bl.model.Complaint;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.ReferralReport;
import kaamverse.bl.service.AudioPromptService;
import kaamverse.bl.service.ComplaintReviewService;
import kaamverse.bl.service.ComplaintReviewService.ComplaintCase;
import kaamverse.bl.service.ComplaintReviewService.ComplaintRow;
import kaamverse.bl.service.ComplaintService;
import kaamverse.bl.service.ComplaintService.EmployerHire;
import kaamverse.bl.service.LoggingAudioPromptService;
import kaamverse.bl.service.Session;
import kaamverse.bl.service.SpeechToText;
import kaamverse.bl.service.StubSpeechToText;
import kaamverse.common.ComplaintOutcome;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.MaidStatus;
import kaamverse.common.Permission;
import kaamverse.common.ProblemType;
import kaamverse.common.StringsB;

public class ComplaintController {

    public static final int MAX_TRANSCRIPT = 5000;
    public static final int MAX_NOTE = 2000;
    public static final int MAX_MESSAGE = 1000;
    public static final int MAX_REASON = 1000;

    private final ComplaintService service = new ComplaintService();
    private final ComplaintReviewService reviewService = new ComplaintReviewService();
    private final AudioPromptService audio = new LoggingAudioPromptService();
    private final SpeechToText speech = new StubSpeechToText();

    // the UC11 draft; nothing is saved until confirmComplaint()
    private Maid maid;
    private List<EmployerHire> choices = new ArrayList<>();
    private boolean agreed;
    private EmployerHire chosen;
    private ProblemType problemType;
    private String transcript;
    private Path audioFile;
    private LocalDate incidentDate;
    private Path evidenceFile;
    private Complaint saved;

    // UC11 step 1: checks she may complain and returns the confidentiality text
    public String reportProblem() {
        maid = loadEligibleMaid();
        choices = service.employerHires(maid.getMaidId());
        if (choices.isEmpty()) {
            throw new ValidationException(StringsB.UC11_ERR_NO_EMPLOYER);
        }
        agreed = false;
        clearDraft();
        saved = null;
        return StringsB.UC11_CONFIDENTIALITY;
    }

    public void playPrompt(String text) {
        audio.play(text);
    }

    public void agreeToContinue(boolean ticked) {
        requireDraft();
        if (!ticked) {
            throw new ValidationException(StringsB.UC11_ERR_NOT_AGREED);
        }
        agreed = true;
    }

    public List<HiringWoman> getEmployers() {
        requireDraft();
        List<HiringWoman> employers = new ArrayList<>();
        for (EmployerHire choice : choices) {
            employers.add(choice.employer());
        }
        return employers;
    }

    public void enterEmployerAndProblemType(Integer employerId, ProblemType type) {
        requireAgreed();
        if (employerId == null) {
            throw new ValidationException(StringsB.UC11_ERR_EMPLOYER);
        }
        EmployerHire found = choices.stream()
                .filter(choice -> choice.employer().getHiringWomanId() == employerId)
                .findFirst()
                .orElseThrow(() -> new ValidationException(StringsB.UC11_ERR_EMPLOYER_GONE));
        if (type == null) {
            throw new ValidationException(StringsB.UC11_ERR_TYPE);
        }
        chosen = found;
        problemType = type;
    }

    // typed text wins; otherwise the audio goes to SpeechToText. Returns the text kept.
    public String describeIncident(Path audioPath, String typedText) {
        requireChosen();
        String typed = typedText == null ? "" : typedText.trim();
        if (audioPath == null && typed.isEmpty()) {
            throw new ValidationException(StringsB.UC11_ERR_DESCRIBE);
        }
        if (typed.length() > MAX_TRANSCRIPT) {
            throw new ValidationException(StringsB.UC11_ERR_TOO_LONG.formatted(MAX_TRANSCRIPT));
        }
        if (audioPath != null && !Files.isReadable(audioPath)) {
            throw new ValidationException(StringsB.UC11_ERR_AUDIO_FILE);
        }
        String text = typed.isEmpty() ? speech.transcribe(audioPath) : typed;
        if (text.length() > MAX_TRANSCRIPT) {
            text = text.substring(0, MAX_TRANSCRIPT);
        }
        audioFile = audioPath;
        transcript = text;
        return text;
    }

    public void enterDateAndEvidence(LocalDate date, Path evidencePath) {
        requireChosen();
        if (transcript == null) {
            throw new ValidationException(StringsB.UC11_ERR_DESCRIBE);
        }
        if (date == null) {
            throw new ValidationException(StringsB.UC11_ERR_DATE);
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(StringsB.UC11_ERR_DATE_FUTURE);
        }
        if (evidencePath != null && !Files.isReadable(evidencePath)) {
            throw new ValidationException(StringsB.UC11_ERR_EVIDENCE_FILE);
        }
        incidentDate = date;
        evidenceFile = evidencePath;
    }

    // saves the complaint, then forgets the draft
    public Complaint confirmComplaint() {
        requireDraft();
        if (saved != null) {
            throw new IllegalStateException("This complaint was already submitted");
        }
        if (chosen == null || problemType == null || transcript == null || incidentDate == null) {
            throw new ValidationException(StringsB.UC11_ERR_INCOMPLETE);
        }
        Maid current = loadEligibleMaid();
        int employerId = chosen.employer().getHiringWomanId();
        // her hires may have changed since she chose the employer
        EmployerHire fresh = service.employerHires(current.getMaidId()).stream()
                .filter(choice -> choice.employer().getHiringWomanId() == employerId)
                .findFirst()
                .orElseThrow(() -> new ValidationException(StringsB.UC11_ERR_EMPLOYER_GONE));
        try {
            saved = service.submit(current, fresh.hire(), problemType, transcript, audioFile,
                    evidenceFile, incidentDate);
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        clearDraft();
        return saved;
    }

    public String getEmployerName() {
        requireChosen();
        return chosen.employer().getName();
    }

    public ProblemType getProblemType() {
        return problemType;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getAudioName() {
        return audioFile == null ? "-" : audioFile.getFileName().toString();
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public String getEvidenceName() {
        return evidenceFile == null ? "-" : evidenceFile.getFileName().toString();
    }

    public String getReferenceNo() {
        if (saved == null) {
            throw new IllegalStateException("The complaint has not been submitted");
        }
        return saved.getReferenceNo();
    }

    // UC12 step 1: open complaints, urgent first (or all of them)
    public List<ComplaintRow> selectComplaints(boolean includeFinished) {
        requireOfficer(Permission.REVIEW_COMPLAINTS);
        return reviewService.list(includeFinished);
    }

    // UC12 step 2: transcript, audio, evidence, incident details, notes and referral
    public ComplaintCase openComplaint(int complaintId) {
        requireOfficer(Permission.REVIEW_COMPLAINTS);
        return guard(() -> reviewService.load(complaintId));
    }

    public void setStatusUnderReview(int complaintId, String investigationNote) {
        Officer officer = requireOfficer(Permission.REVIEW_COMPLAINTS);
        String note = investigationNote == null ? "" : investigationNote.trim();
        if (note.isEmpty()) {
            throw new ValidationException(StringsB.UC12_ERR_NOTE_EMPTY);
        }
        if (note.length() > MAX_NOTE) {
            throw new ValidationException(StringsB.UC12_ERR_NOTE_LONG.formatted(MAX_NOTE));
        }
        guard(() -> {
            reviewService.startReview(officer, complaintId, note);
            return null;
        });
    }

    public void requestClarification(int complaintId, String message) {
        Officer officer = requireOfficer(Permission.REVIEW_COMPLAINTS);
        String text = message == null ? "" : message.trim();
        if (text.isEmpty()) {
            throw new ValidationException(StringsB.UC12_ERR_MESSAGE_EMPTY);
        }
        if (text.length() > MAX_MESSAGE) {
            throw new ValidationException(StringsB.UC12_ERR_MESSAGE_LONG.formatted(MAX_MESSAGE));
        }
        guard(() -> {
            reviewService.requestClarification(officer, complaintId, text);
            return null;
        });
    }

    // closing outcomes close the complaint and return no authorities; escalation returns the list to choose from
    public List<String> decideOutcome(int complaintId, ComplaintOutcome outcome) {
        Officer officer = requireOfficer(Permission.ESCALATE_COMPLAINTS);
        if (outcome == null) {
            throw new ValidationException(StringsB.UC12_ERR_OUTCOME);
        }
        return guard(() -> {
            if (outcome.isEscalation()) {
                reviewService.checkCanDecide(complaintId);
                return reviewService.authorities();
            }
            reviewService.close(officer, complaintId, outcome);
            return List.of();
        });
    }

    public ReferralReport escalateComplaint(int complaintId, String authority, String referralReason) {
        Officer officer = requireOfficer(Permission.ESCALATE_COMPLAINTS);
        if (authority == null || authority.isBlank()) {
            throw new ValidationException(StringsB.UC12_ERR_AUTHORITY);
        }
        if (!reviewService.authorities().contains(authority)) {
            throw new ValidationException(StringsB.UC12_ERR_AUTHORITY_UNKNOWN);
        }
        String reason = referralReason == null ? "" : referralReason.trim();
        if (reason.isEmpty()) {
            throw new ValidationException(StringsB.UC12_ERR_REASON_EMPTY);
        }
        if (reason.length() > MAX_REASON) {
            throw new ValidationException(StringsB.UC12_ERR_REASON_LONG.formatted(MAX_REASON));
        }
        return guard(() -> reviewService.escalate(officer, complaintId, authority, reason));
    }

    // turns the service's ready-to-show messages into ValidationException
    private <T> T guard(Supplier<T> action) {
        try {
            return action.get();
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        } catch (DuplicateRecordException e) {
            throw new ValidationException(StringsB.UC12_ERR_CHANGED);
        }
    }

    private static Officer requireOfficer(Permission permission) {
        if (Session.isLoggedIn() && Session.getUser() instanceof Officer officer
                && officer.getPermissions().contains(permission)) {
            return officer;
        }
        throw new ValidationException(StringsB.UC12_ERR_NOT_ALLOWED);
    }

    private void clearDraft() {
        chosen = null;
        problemType = null;
        transcript = null;
        audioFile = null;
        incidentDate = null;
        evidenceFile = null;
    }

    // the maid as she is in the database now, if she may file a complaint
    private Maid loadEligibleMaid() {
        Maid current = requireMaid();
        Maid fresh = service.findMaid(current.getMaidId())
                .orElseThrow(() -> new ValidationException(StringsB.UC11_ERR_NOT_ALLOWED));
        if (fresh.getStatus() != MaidStatus.Verified) {
            throw new ValidationException(StringsB.UC11_ERR_NOT_VERIFIED);
        }
        return fresh;
    }

    private static Maid requireMaid() {
        if (Session.isLoggedIn() && Session.getUser() instanceof Maid current) {
            return current;
        }
        throw new ValidationException(StringsB.UC11_ERR_NOT_ALLOWED);
    }

    private void requireDraft() {
        if (maid == null) {
            throw new IllegalStateException("reportProblem() has not been called");
        }
    }

    private void requireAgreed() {
        requireDraft();
        if (!agreed) {
            throw new ValidationException(StringsB.UC11_ERR_NOT_AGREED);
        }
    }

    private void requireChosen() {
        requireAgreed();
        if (chosen == null) {
            throw new ValidationException(StringsB.UC11_ERR_EMPLOYER);
        }
    }
}