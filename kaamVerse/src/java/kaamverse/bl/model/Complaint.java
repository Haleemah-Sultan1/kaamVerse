package kaamverse.bl.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kaamverse.common.ComplaintStatus;

/** A confidential complaint. GRASP Creator for notes and referrals. */
public class Complaint {

    private int complaintId;
    private String referenceNo;
    private int maidId;
    private int hireId;
    private String problemType;
    private String urgency;
    private String transcript;
    private String audioPath;
    private String evidencePath;
    private LocalDate incidentDate;
    private ComplaintStatus status;
    private String outcome;
    private String clarificationMessage;
    private LocalDate filedDate;

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public int getMaidId() {
        return maidId;
    }

    public void setMaidId(int maidId) {
        this.maidId = maidId;
    }

    public int getHireId() {
        return hireId;
    }

    public void setHireId(int hireId) {
        this.hireId = hireId;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(String evidencePath) {
        this.evidencePath = evidencePath;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getClarificationMessage() {
        return clarificationMessage;
    }

    public void setClarificationMessage(String clarificationMessage) {
        this.clarificationMessage = clarificationMessage;
    }

    public LocalDate getFiledDate() {
        return filedDate;
    }

    public void setFiledDate(LocalDate filedDate) {
        this.filedDate = filedDate;
    }

    public InvestigationNote setUnderReview(int officerId, String text) {
        status = ComplaintStatus.UnderReview;
        InvestigationNote note = new InvestigationNote();
        note.setComplaintId(complaintId);
        note.setOfficerId(officerId);
        note.setText(text);
        note.setNoteDate(LocalDateTime.now());
        return note;
    }

    public ReferralReport escalate(String authority, String reason) {
        status = ComplaintStatus.Escalated;
        ReferralReport report = new ReferralReport();
        report.setComplaintId(complaintId);
        report.setAuthority(authority);
        report.setReason(reason);
        report.setGeneratedDate(LocalDate.now());
        return report;
    }
}
