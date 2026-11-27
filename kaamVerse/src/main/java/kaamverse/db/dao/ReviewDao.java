package kaamverse.db.dao;

import kaamverse.bl.model.Review;

public interface ReviewDao {

    // saves a new review, sets its ID and returns it
    int insert(Review review);

    boolean existsForHire(int hireId);
}