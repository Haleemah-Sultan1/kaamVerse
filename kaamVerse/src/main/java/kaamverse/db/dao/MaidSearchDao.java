package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.Maid;
import kaamverse.bl.model.MaidSearchFilter;
import kaamverse.bl.model.MaidService;
import kaamverse.bl.model.Review;

public interface MaidSearchDao {

    // verified maids only, best rated first
    List<Maid> findMatching(MaidSearchFilter filter);

    List<MaidService> findServices(int maidId);

    List<Review> findApprovedReviews(int maidId);
}