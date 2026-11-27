package kaamverse.bl.model;

import java.time.LocalDate;

/** Badge awarded to a maid. */
public class TrustBadge {

    private int badgeId;
    private int maidId;
    private LocalDate awardedDate;
    private String reason;
    private int awardedByOfficerId;

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public int getMaidId() {
        return maidId;
    }

    public void setMaidId(int maidId) {
        this.maidId = maidId;
    }

    public LocalDate getAwardedDate() {
        return awardedDate;
    }

    public void setAwardedDate(LocalDate awardedDate) {
        this.awardedDate = awardedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getAwardedByOfficerId() {
        return awardedByOfficerId;
    }

    public void setAwardedByOfficerId(int awardedByOfficerId) {
        this.awardedByOfficerId = awardedByOfficerId;
    }
}
