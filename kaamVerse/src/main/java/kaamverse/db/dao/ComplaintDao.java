package kaamverse.db.dao;

import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Complaint;
import kaamverse.common.ComplaintStatus;

public interface ComplaintDao {

    int insert(Complaint complaint);

    Optional<String> findLatestReferenceNo(String prefix);

    Optional<Complaint> findById(int complaintId);

    // urgent first, then oldest first
    List<Complaint> findByStatuses(List<ComplaintStatus> statuses);

    // saves status, outcome and clarification; false if the status is no longer one of allowedFrom
    boolean updateDecision(Complaint complaint, List<ComplaintStatus> allowedFrom);
}