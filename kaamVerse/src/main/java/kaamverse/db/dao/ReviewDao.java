package kaamverse.db.dao;

import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Review;
import kaamverse.common.ReviewStatus;

public interface ReviewDao {

    // saves a new review, sets its ID and returns it
    int insert(Review review);

    boolean existsForHire(int hireId);

    // UC5: reviews waiting for the moderator (Pending and flagged), oldest first
    List<Review> findPendingFlagged();

    Optional<Review> findById(int reviewId);

    // UC5: Pending -> newStatus. Returns false if the review was already decided.
    boolean decide(int reviewId, ReviewStatus newStatus);

    // every approved review of one maid, newest first
    List<Review> findApprovedByMaid(int maidId);

    // IDs of the maids who have at least one approved review
    List<Integer> findMaidIdsWithApprovedReviews();

    // the average is worked out from approved reviews (Maid.updateAverageRating) and saved here
    void saveMaidAverage(int maidId, double average);
}