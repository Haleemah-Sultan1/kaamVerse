package kaamverse.bl.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Hire;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Review;
import kaamverse.common.HireStatus;
import kaamverse.common.ReviewStatus;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.DaoFactoryB;
import kaamverse.db.dao.HireDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.ReviewDao;

// UC4 data access and saving. High Cohesion: past hires and their reviews only.
public class ExperienceService {

    public record PastHire(Hire hire, String maidName, boolean reviewed) {
    }

    private final HireDao hireDao = DaoFactoryB.hireDao();
    private final ReviewDao reviewDao = DaoFactoryB.reviewDao();
    private final MaidDao maidDao = DaoFactory.maidDao();

    public List<PastHire> endedHires(int womanId) {
        List<PastHire> result = new ArrayList<>();
        for (Hire hire : hireDao.findByWoman(womanId)) {
            if (hire.getStatus() == HireStatus.Ended) {
                String name = maidDao.findById(hire.getMaidId()).map(Maid::getName).orElse("");
                result.add(new PastHire(hire, name, reviewDao.existsForHire(hire.getHireId())));
            }
        }
        return result;
    }

    public Optional<PastHire> findEndedHire(int womanId, int hireId) {
        return endedHires(womanId).stream().filter(item -> item.hire().getHireId() == hireId).findFirst();
    }

    // every new review is flagged and Pending; UC5 approves it, and only then does the average change
    public Review saveReview(int hireId, int overall, int punctuality, int honesty, int cleanliness,
                             int behaviour, boolean wouldRehire, String comment) {
        Review review = new Review();
        review.setHireId(hireId);
        review.setOverallRating(overall);
        review.setPunctuality(punctuality);
        review.setHonesty(honesty);
        review.setCleanliness(cleanliness);
        review.setBehaviour(behaviour);
        review.setWouldRehire(wouldRehire);
        review.setComment(comment);
        review.setFlagged(true);
        review.setStatus(ReviewStatus.Pending);
        review.setCreatedDate(LocalDate.now());
        reviewDao.insert(review);
        return review;
    }
}