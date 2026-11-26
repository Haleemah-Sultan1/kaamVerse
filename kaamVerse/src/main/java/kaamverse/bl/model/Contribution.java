package kaamverse.bl.model;

import java.time.LocalDate;

/** Money pledged towards a funding request. */
public class Contribution {

    private int contributionId;
    private int requestId;
    private int hiringWomanId;
    private double amount;
    private String note;
    private LocalDate contributionDate;
    private String acknowledgementNo;

    public int getContributionId() {
        return contributionId;
    }

    public void setContributionId(int contributionId) {
        this.contributionId = contributionId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getHiringWomanId() {
        return hiringWomanId;
    }

    public void setHiringWomanId(int hiringWomanId) {
        this.hiringWomanId = hiringWomanId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(LocalDate contributionDate) {
        this.contributionDate = contributionDate;
    }

    public String getAcknowledgementNo() {
        return acknowledgementNo;
    }

    public void setAcknowledgementNo(String acknowledgementNo) {
        this.acknowledgementNo = acknowledgementNo;
    }
}
