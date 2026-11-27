package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.FundingRecipient;

/** Saves and reads the employers a funding request was sent to. */
public interface FundingRecipientDao {

    /** UC9: saves all recipients of a request (their request ID must already be set). */
    void insertAll(List<FundingRecipient> recipients);

    /** UC10: true if the request was sent to this hiring woman. */
    boolean isRecipient(int requestId, int hiringWomanId);
}
