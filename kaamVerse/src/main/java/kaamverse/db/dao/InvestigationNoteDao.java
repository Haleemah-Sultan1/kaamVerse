package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.InvestigationNote;

public interface InvestigationNoteDao {

    int insert(InvestigationNote note);

    // newest first
    List<InvestigationNote> findByComplaint(int complaintId);
}