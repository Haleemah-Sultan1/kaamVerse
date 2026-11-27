package kaamverse.bl.service;

/** GRASP Pure Fabrication: decides who may get a trust badge. */
public interface BadgeEligibilityPolicy {

    boolean isEligible(int approvedReviewCount, double averageRating, double positivePercentage);
}
