package kaamverse.bl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.AuditLogEntry;
import kaamverse.bl.model.Complaint;
import kaamverse.bl.model.Hire;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.InvestigationNote;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.ReferralReport;
import kaamverse.common.ComplaintOutcome;
import kaamverse.common.ComplaintStatus;
import kaamverse.common.DatabaseException;
import kaamverse.common.StringsB;
import kaamverse.db.dao.ComplaintDao;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.DaoFactoryB;
import kaamverse.db.dao.HireDao;
import kaamverse.db.dao.HiringWomanDao;
import kaamverse.db.dao.InvestigationNoteDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.OfficerDao;
import kaamverse.db.dao.ReferralReportDao;
import kaamverse.db.dao.TransactionManager;

public class ComplaintReviewService {

    public record ComplaintRow(Complaint complaint, String maidName, String employerName) {
    }

    public record NoteLine(InvestigationNote note, String officerName) {
    }

    public record ComplaintCase(Complaint complaint, String maidName, String employerName,
                                List<NoteLine> notes, Optional<ReferralReport> referral) {
    }

    private static final List<String> AUTHORITIES = List.of("Local Police Station",
            "Women Protection Authority", "Labour Department", "Harassment Ombudsperson");
    private static final List<ComplaintStatus> OPEN = List.of(ComplaintStatus.Filed, ComplaintStatus.UnderReview);
    private static final List<ComplaintStatus> ALL = List.of(ComplaintStatus.Filed, ComplaintStatus.UnderReview,
            ComplaintStatus.Escalated, ComplaintStatus.Closed);
    private static final List<ComplaintStatus> UNDER_REVIEW = List.of(ComplaintStatus.UnderReview);

    private final ComplaintDao complaintDao = DaoFactoryB.complaintDao();
    private final InvestigationNoteDao noteDao = DaoFactoryB.investigationNoteDao();
    private final ReferralReportDao referralDao = DaoFactoryB.referralReportDao();
    private final HireDao hireDao = DaoFactoryB.hireDao();
    private final MaidDao maidDao = DaoFactory.maidDao();
    private final HiringWomanDao womanDao = DaoFactory.hiringWomanDao();
    private final OfficerDao officerDao = DaoFactory.officerDao();
    private final NotificationService notifications = new NotificationService();

    public List<String> authorities() {
        return AUTHORITIES;
    }

    public List<ComplaintRow> list(boolean includeFinished) {
        List<ComplaintRow> rows = new ArrayList<>();
        for (Complaint complaint : complaintDao.findByStatuses(includeFinished ? ALL : OPEN)) {
            rows.add(new ComplaintRow(complaint, maidName(complaint), employerName(complaint)));
        }
        return rows;
    }

    public ComplaintCase load(int complaintId) {
        Complaint complaint = find(complaintId);
        List<NoteLine> notes = new ArrayList<>();
        for (InvestigationNote note : noteDao.findByComplaint(complaintId)) {
            String officer = officerDao.findById(note.getOfficerId()).map(Officer::getName).orElse("");
            notes.add(new NoteLine(note, officer));
        }
        return new ComplaintCase(complaint, maidName(complaint), employerName(complaint), notes,
                referralDao.findByComplaint(complaintId));
    }

    // saves the note and sets Under Review; more notes can be added while it stays Under Review
    public void startReview(Officer officer, int complaintId, String noteText) {
        boolean[] first = new boolean[1];
        Complaint saved = TransactionManager.call(() -> {
            Complaint complaint = find(complaintId);
            if (complaint.getStatus() != ComplaintStatus.Filed && complaint.getStatus() != ComplaintStatus.UnderReview) {
                throw new IllegalStateException(StringsB.UC12_ERR_NOT_OPEN);
            }
            first[0] = complaint.getStatus() == ComplaintStatus.Filed;
            InvestigationNote note = complaint.setUnderReview(officer.getOfficerId(), noteText);
            save(complaint, OPEN);
            noteDao.insert(note);
            AuditLogEntry.log(officer, (first[0] ? StringsB.UC12_AUDIT_REVIEW : StringsB.UC12_AUDIT_NOTE)
                    .formatted(complaint.getReferenceNo()));
            return complaint;
        });
        if (first[0]) {
            notifyMaid(saved, StringsB.UC12_NOTIFY_REVIEW.formatted(saved.getReferenceNo()));
        }
    }

    public void requestClarification(Officer officer, int complaintId, String message) {
        Complaint saved = TransactionManager.call(() -> {
            Complaint complaint = find(complaintId);
            requireUnderReview(complaint);
            complaint.setClarificationMessage(message);
            save(complaint, UNDER_REVIEW);
            AuditLogEntry.log(officer, StringsB.UC12_AUDIT_CLARIFY.formatted(complaint.getReferenceNo()));
            return complaint;
        });
        notifyMaid(saved, StringsB.UC12_NOTIFY_CLARIFY.formatted(saved.getReferenceNo(), message));
    }

    // checks that the complaint can still be decided before the officer picks an authority
    public void checkCanDecide(int complaintId) {
        requireUnderReview(find(complaintId));
    }

    public void close(Officer officer, int complaintId, ComplaintOutcome outcome) {
        Complaint saved = TransactionManager.call(() -> {
            Complaint complaint = find(complaintId);
            requireUnderReview(complaint);
            complaint.setStatus(ComplaintStatus.Closed);
            complaint.setOutcome(outcome.getLabel());
            save(complaint, UNDER_REVIEW);
            AuditLogEntry.log(officer, StringsB.UC12_AUDIT_CLOSED.formatted(
                    complaint.getReferenceNo(), outcome.getLabel()));
            return complaint;
        });
        notifyMaid(saved, StringsB.UC12_NOTIFY_CLOSED.formatted(saved.getReferenceNo(), outcome.getLabel()));
    }

    public ReferralReport escalate(Officer officer, int complaintId, String authority, String reason) {
        Complaint[] changed = new Complaint[1];
        ReferralReport report = TransactionManager.call(() -> {
            Complaint complaint = find(complaintId);
            requireUnderReview(complaint);
            // GRASP Creator: the complaint creates its own referral report
            ReferralReport created = complaint.escalate(authority, reason);
            complaint.setOutcome(StringsB.UC12_OUTCOME_ESCALATED.formatted(authority));
            save(complaint, UNDER_REVIEW);
            referralDao.insert(created);
            AuditLogEntry.log(officer, StringsB.UC12_AUDIT_ESCALATED.formatted(
                    complaint.getReferenceNo(), authority));
            changed[0] = complaint;
            return created;
        });
        notifyMaid(changed[0], StringsB.UC12_NOTIFY_ESCALATED.formatted(changed[0].getReferenceNo(), authority));
        return report;
    }

    private Complaint find(int complaintId) {
        return complaintDao.findById(complaintId)
                .orElseThrow(() -> new IllegalStateException(StringsB.UC12_ERR_NOT_FOUND));
    }

    private static void requireUnderReview(Complaint complaint) {
        if (complaint.getStatus() == ComplaintStatus.UnderReview) {
            return;
        }
        if (complaint.getStatus() == ComplaintStatus.Filed) {
            throw new IllegalStateException(StringsB.UC12_ERR_NOT_UNDER_REVIEW);
        }
        throw new IllegalStateException(StringsB.UC12_ERR_NOT_OPEN);
    }

    // the update only applies if nobody changed the status in the meantime
    private void save(Complaint complaint, List<ComplaintStatus> allowedFrom) {
        if (!complaintDao.updateDecision(complaint, allowedFrom)) {
            throw new IllegalStateException(StringsB.UC12_ERR_CHANGED);
        }
    }

    private String maidName(Complaint complaint) {
        return maidDao.findById(complaint.getMaidId()).map(maid -> maid.getName()).orElse("");
    }

    private String employerName(Complaint complaint) {
        Optional<Hire> hire = hireDao.findById(complaint.getHireId());
        if (hire.isEmpty()) {
            return "";
        }
        return womanDao.findById(hire.get().getHiringWomanId()).map(HiringWoman::getName).orElse("");
    }

    // the change is already saved; a lost notification must not undo it
    private void notifyMaid(Complaint complaint, String text) {
        try {
            maidDao.findById(complaint.getMaidId()).ifPresent(maid -> notifications.send(maid, text));
        } catch (DatabaseException e) {
            System.err.println("Could not notify the maid: " + e.getMessage());
        }
    }
}