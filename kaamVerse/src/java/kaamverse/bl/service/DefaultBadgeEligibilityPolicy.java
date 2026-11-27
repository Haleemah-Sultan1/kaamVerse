package kaamverse.bl.service;

/** At least 5 approved reviews, average 4.0 or more, and 80% or more rated 4+. */
public class DefaultBadgeEligibilityPolicy implements BadgeEligibilityPolicy {

    private static final int MIN_REVIEWS = 5;
    private static final double MIN_AVERAGE = 4.0;
    private static final double MIN_POSITIVE_PERCENTAGE = 80.0;

    /** positivePercentage is on a 0 to 100 scale. */
    @Override
    public boolean isEligible(int approvedReviewCount, double averageRating, double positivePercentage) {
        return approvedReviewCount >= MIN_REVIEWS
                && averageRating >= MIN_AVERAGE
                && positivePercentage >= MIN_POSITIVE_PERCENTAGE;
    }
}
