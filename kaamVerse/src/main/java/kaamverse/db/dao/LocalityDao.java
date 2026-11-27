package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.Locality;

/** Reads localities. */
public interface LocalityDao {

    /** All localities by name; their streets are not loaded. */
    List<Locality> findAll();
}
