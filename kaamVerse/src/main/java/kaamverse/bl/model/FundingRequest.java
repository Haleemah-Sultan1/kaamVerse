package kaamverse.bl.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kaamverse.common.FundingStatus;

/** A request for help with an event. GRASP Creator and Information Expert for contributions. */
public class FundingRequest {

    private int requestId;
    private int maidId;
    private String trackingNo;
    private String eventType;
    private double amountNeeded;
    private double amountReceived;
    private LocalDate eventDate;
    private String reason;
    private FundingStatus status;
    private LocalDate createdDate;
    private List<FundingRecipient> recipients = new ArrayList<>();
    private List<Contribution> contributions = new ArrayList<>();

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

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public double getAmountNeeded() {
        return amountNeeded;
    }

    public void setAmountNeeded(double amountNeeded) {
        this.amountNeeded = amountNeeded;
    }

    public double getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(double amountReceived) {
        this.amountReceived = amountReceived;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public FundingStatus getStatus() {
        return status;
    }

    public void setStatus(FundingStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public List<FundingRecipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<FundingRecipient> recipients) {
        this.recipients = recipients;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    public void sendTo(List<HiringWoman> employers) {
        for (HiringWoman employer : employers) {
            FundingRecipient recipient = new FundingRecipient();
            recipient.setRequestId(requestId);
            recipient.setHiringWomanId(employer.getHiringWomanId());
            recipients.add(recipient);
        }
    }

    /** Records a contribution and updates the amount received and status. */
    public Contribution addContribution(HiringWoman contributor, double amount, String note) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        Contribution contribution = new Contribution();
        contribution.setRequestId(requestId);
        contribution.setHiringWomanId(contributor.getHiringWomanId());
        contribution.setAmount(amount);
        contribution.setNote(note);
        contribution.setContributionDate(LocalDate.now());
        contributions.add(contribution);
        amountReceived += amount;
        status = amountReceived >= amountNeeded ? FundingStatus.Funded : FundingStatus.PartiallyFunded;
        return contribution;
    }
}
