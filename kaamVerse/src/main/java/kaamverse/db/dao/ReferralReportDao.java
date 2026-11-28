package kaamverse.db.dao;

import java.util.Optional;

import kaamverse.bl.model.ReferralReport;

public interface ReferralReportDao {

    // one referral per complaint; a second insert throws DuplicateRecordException
    int insert(ReferralReport report);

    Optional<ReferralReport> findByComplaint(int complaintId);
}