package kaamverse.bl.model;

import java.time.LocalDateTime;

import kaamverse.db.dao.DaoFactory;

/** A record of an officer's action. */
public class AuditLogEntry {

    private int logId;
    private int officerId;
    private String action;
    private LocalDateTime timestamp;

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getOfficerId() {
        return officerId;
    }

    public void setOfficerId(int officerId) {
        this.officerId = officerId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * GRASP Creator: builds an entry for the officer's action and saves it.
     * To save it together with other changes, call it inside TransactionManager.run.
     */
    public static AuditLogEntry log(Officer officer, String action) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setOfficerId(officer.getOfficerId());
        entry.setAction(action);
        entry.setTimestamp(LocalDateTime.now());
        DaoFactory.auditLogDao().insert(entry);
        return entry;
    }
}
