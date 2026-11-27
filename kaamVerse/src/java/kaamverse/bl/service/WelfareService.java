package kaamverse.bl.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import kaamverse.bl.model.AuditLogEntry;
import kaamverse.bl.model.EmergencyContact;
import kaamverse.bl.model.FamilyMember;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Officer;
import kaamverse.common.DatabaseException;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.EmergencyContactDao;
import kaamverse.db.dao.FamilyMemberDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.TransactionManager;

/**
 * UC8 data access and saving. Each change and its audit entry are saved in
 * one transaction; the maid's notification is sent after the commit.
 */
public class WelfareService {

    private static final Logger LOG = Logger.getLogger(WelfareService.class.getName());
    /** Length of audit_log_entry.action. */
    private static final int MAX_ACTION_LENGTH = 255;

    private final MaidDao maidDao = DaoFactory.maidDao();
    private final FamilyMemberDao familyDao = DaoFactory.familyMemberDao();
    private final EmergencyContactDao contactDao = DaoFactory.emergencyContactDao();
    private final NotificationService notificationService = new NotificationService();

    public List<Maid> search(String text) {
        return maidDao.search(text);
    }

    public Optional<Maid> findMaid(int maidId) {
        return maidDao.findById(maidId);
    }

    public List<FamilyMember> familyMembers(int maidId) {
        return familyDao.findByMaid(maidId);
    }

    public List<EmergencyContact> emergencyContacts(int maidId) {
        return contactDao.findByMaid(maidId);
    }

    /** GRASP Creator: the Maid creates the FamilyMember. */
    public FamilyMember addFamilyMember(Officer officer, Maid maid, String name, String relation, int age,
                                        String cnicOrBForm, boolean dependent, String school, String contact,
                                        String audit, String notice) {
        FamilyMember member = maid.addFamilyMember(name, relation, age);
        member.setCnicOrBForm(cnicOrBForm);
        member.setDependent(dependent);
        member.setSchool(school);
        member.setContactNumber(contact);
        TransactionManager.run(() -> {
            familyDao.insert(member);
            AuditLogEntry.log(officer, truncate(audit));
        });
        notifyMaid(maid, notice);
        return member;
    }

    /** @return false if the member no longer exists */
    public boolean updateFamilyMember(Officer officer, Maid maid, FamilyMember member, String audit, String notice) {
        boolean updated = TransactionManager.call(() -> {
            if (!familyDao.update(member)) {
                return false;
            }
            AuditLogEntry.log(officer, truncate(audit));
            return true;
        });
        if (updated) {
            notifyMaid(maid, notice);
        }
        return updated;
    }

    /** @return false if the member no longer exists */
    public boolean deleteFamilyMember(Officer officer, Maid maid, int memberId, String audit, String notice) {
        boolean deleted = TransactionManager.call(() -> {
            if (!familyDao.delete(maid.getMaidId(), memberId)) {
                return false;
            }
            AuditLogEntry.log(officer, truncate(audit));
            return true;
        });
        if (deleted) {
            notifyMaid(maid, notice);
        }
        return deleted;
    }

    /** GRASP Creator: the Maid creates the EmergencyContact. */
    public EmergencyContact addEmergencyContact(Officer officer, Maid maid, String name, String relation,
                                                String phone, String audit, String notice) {
        EmergencyContact contact = maid.addEmergencyContact(name, relation, phone);
        TransactionManager.run(() -> {
            contactDao.insert(contact);
            AuditLogEntry.log(officer, truncate(audit));
        });
        notifyMaid(maid, notice);
        return contact;
    }

    /** @return false if the contact no longer exists */
    public boolean deleteEmergencyContact(Officer officer, Maid maid, int contactId, String audit, String notice) {
        boolean deleted = TransactionManager.call(() -> {
            if (!contactDao.delete(maid.getMaidId(), contactId)) {
                return false;
            }
            AuditLogEntry.log(officer, truncate(audit));
            return true;
        });
        if (deleted) {
            notifyMaid(maid, notice);
        }
        return deleted;
    }

    /** After the commit: one failed message must not make the officer think the change was lost. */
    private void notifyMaid(Maid maid, String text) {
        try {
            notificationService.send(maid, text);
        } catch (DatabaseException e) {
            LOG.log(Level.WARNING, "Could not notify maid " + maid.getMaidId(), e);
        }
    }

    private static String truncate(String action) {
        return action.length() <= MAX_ACTION_LENGTH ? action : action.substring(0, MAX_ACTION_LENGTH);
    }
}
