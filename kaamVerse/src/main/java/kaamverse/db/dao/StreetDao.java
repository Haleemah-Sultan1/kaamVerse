package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.Street;

/** Reads streets. */
public interface StreetDao {

    List<Street> findByLocality(int localityId);
}
