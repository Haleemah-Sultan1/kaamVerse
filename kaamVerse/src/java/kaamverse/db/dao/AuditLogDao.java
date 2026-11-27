package kaamverse.db.dao;

import java.util.List;
import kaamverse.bl.model.AuditLogEntry;

/** Records officer actions. Entries are never changed or deleted. */
public interface AuditLogDao {

    /** Saves an entry, sets its ID and returns it. */
    int insert(AuditLogEntry entry);

    /** One officer's entries, newest first. */
    List<AuditLogEntry> findByOfficer(int officerId);
}
