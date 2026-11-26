package kaamverse.bl.model;

/** A hiring woman who was sent a funding request. */
public class FundingRecipient {

    private int requestId;
    private int hiringWomanId;

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
}
