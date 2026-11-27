package kaamverse.bl.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kaamverse.common.VerificationStatus;

/** A maid's request to have her identity checked. */
public class VerificationRequest {

    private int requestId;
    private int maidId;
    private VerificationStatus status;
    private String rejectionReason;
    private Integer verifiedByOfficerId;
    private LocalDateTime verifiedDateTime;
    private LocalDate submittedDate;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getMaidId() {
        return maidId;
    }

    public void setMaidId(int maidId) {
        this.maidId = maidId;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Integer getVerifiedByOfficerId() {
        return verifiedByOfficerId;
    }

    public void setVerifiedByOfficerId(Integer verifiedByOfficerId) {
        this.verifiedByOfficerId = verifiedByOfficerId;
    }

    public LocalDateTime getVerifiedDateTime() {
        return verifiedDateTime;
    }

    public void setVerifiedDateTime(LocalDateTime verifiedDateTime) {
        this.verifiedDateTime = verifiedDateTime;
    }

    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDate submittedDate) {
        this.submittedDate = submittedDate;
    }

    public void markVerified(int officerId) {
        status = VerificationStatus.Matched;
        verifiedByOfficerId = officerId;
        verifiedDateTime = LocalDateTime.now();
    }

    /** GRASP Information Expert: the request records its own rejection (reason, officer, time). */
    public void markRejected(int officerId, String reason) {
        status = VerificationStatus.Rejected;
        rejectionReason = reason;
        verifiedByOfficerId = officerId;
        verifiedDateTime = LocalDateTime.now();
    }
}
