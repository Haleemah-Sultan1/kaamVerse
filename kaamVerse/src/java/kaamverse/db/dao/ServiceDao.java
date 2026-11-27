package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.Service;

/** Reads services. */
public interface ServiceDao {

    /** All services by name; their wage bands are not loaded. */
    List<Service> findAll();
}
