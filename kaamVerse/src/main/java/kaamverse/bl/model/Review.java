package kaamverse.bl.model;

import java.time.LocalDate;

import kaamverse.common.ReviewStatus;

/** Feedback on one hire. Ratings are 1 to 5. */
public class Review {

    private int reviewId;
    private int hireId;
    private int overallRating;
    private int punctuality;
    private int honesty;
    private int cleanliness;
    private int behaviour;
    private boolean wouldRehire;
    private String comment;
    private boolean isFlagged;
    private ReviewStatus status;
    private LocalDate createdDate;

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getHireId() {
        return hireId;
    }

    public void setHireId(int hireId) {
        this.hireId = hireId;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }

    public int getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(int punctuality) {
        this.punctuality = punctuality;
    }

    public int getHonesty() {
        return honesty;
    }

    public void setHonesty(int honesty) {
        this.honesty = honesty;
    }

    public int getCleanliness() {
        return cleanliness;
    }

    public void setCleanliness(int cleanliness) {
        this.cleanliness = cleanliness;
    }

    public int getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(int behaviour) {
        this.behaviour = behaviour;
    }

    public boolean isWouldRehire() {
        return wouldRehire;
    }

    public void setWouldRehire(boolean wouldRehire) {
        this.wouldRehire = wouldRehire;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public void approve() {
        status = ReviewStatus.Approved;
    }
}
