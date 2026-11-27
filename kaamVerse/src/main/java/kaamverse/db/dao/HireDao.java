package kaamverse.db.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Hire;
import kaamverse.common.HireStatus;

public interface HireDao {

    // saves the hire and its service lines, sets the hire ID and returns it
    int insert(Hire hire);

    Optional<Hire> findById(int hireId);

    // newest first; service lines are not loaded
    List<Hire> findByWoman(int hiringWomanId);

    // newest first; service lines are not loaded
    List<Hire> findByMaid(int maidId);
    void updateStatus(int hireId, HireStatus status, LocalDate startDate, LocalDate endDate);
}