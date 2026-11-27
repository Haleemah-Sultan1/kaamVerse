package kaamverse.db.dao;

import java.util.Optional;

import kaamverse.bl.model.Complaint;

public interface ComplaintDao {

    int insert(Complaint complaint);

    Optional<String> findLatestReferenceNo(String prefix);
}