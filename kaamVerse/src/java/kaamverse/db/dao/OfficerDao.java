package kaamverse.db.dao;

import java.util.Optional;
import kaamverse.bl.model.Officer;

/** Reads officers. Each row becomes the matching Officer subclass. */
public interface OfficerDao {

    Optional<Officer> findById(int officerId);

    Optional<Officer> findByUsername(String username);
}
