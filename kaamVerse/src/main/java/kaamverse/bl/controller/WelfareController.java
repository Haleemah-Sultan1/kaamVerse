package kaamverse.bl.controller;

import java.util.List;

import kaamverse.bl.model.EmergencyContact;
import kaamverse.bl.model.FamilyMember;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Officer;
import kaamverse.bl.service.Session;
import kaamverse.bl.service.WelfareService;
import kaamverse.common.Permission;
import kaamverse.common.Strings;

/**
 * GRASP Controller for UC8 Maintain Maid Family Records. The screen talks
 * only to this class. It checks the officer's permission and all input on
 * every call, because the menu only hides the button.
 * Pattern: searchMaids, getFamilyMembers / getEmergencyContacts, add / edit / delete.
 */
public class WelfareController {

    private static final int MAX_NAME = 100;
    private static final int MAX_RELATION = 50;
    private static final int MAX_SCHOOL = 150;
    private static final int MAX_ID_NUMBER = 15;

    private final WelfareService service = new WelfareService();

    public List<Maid> searchMaids(String text) {
        requireWelfareOfficer();
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(Strings.UC8_ERR_SEARCH);
        }
        return service.search(trimmed);
    }

    public List<FamilyMember> getFamilyMembers(int maidId) {
        requireWelfareOfficer();
        return service.familyMembers(maidId);
    }

    public List<EmergencyContact> getEmergencyContacts(int maidId) {
        requireWelfareOfficer();
        return service.emergencyContacts(maidId);
    }

    public void addFamilyMember(int maidId, FamilyMemberForm form) {
        Officer officer = requireWelfareOfficer();
        Valid valid = check(form);
        Maid maid = requireMaid(maidId);
        service.addFamilyMember(officer, maid, valid.name, valid.relation, valid.age, valid.idNumber,
                form.dependent(), valid.school, valid.contact,
                Strings.UC8_AUDIT_ADD_FAMILY.formatted(valid.name, valid.relation, maidId),
                Strings.UC8_NOTIFY_ADD_FAMILY.formatted(valid.name, valid.relation));
    }

    public void editFamilyMember(int maidId, int memberId, FamilyMemberForm form) {
        Officer officer = requireWelfareOfficer();
        Valid valid = check(form);
        Maid maid = requireMaid(maidId);
        FamilyMember member = service.familyMembers(maidId).stream()
                .filter(existing -> existing.getFamilyMemberId() == memberId)
                .findFirst()
                .orElseThrow(() -> new ValidationException(Strings.UC8_ERR_RECORD_GONE));
        member.setName(valid.name);
        member.setRelation(valid.relation);
        member.setAge(valid.age);
        member.setCnicOrBForm(valid.idNumber);
        member.setDependent(form.dependent());
        member.setSchool(valid.school);
        member.setContactNumber(valid.contact);
        if (!service.updateFamilyMember(officer, maid, member,
                Strings.UC8_AUDIT_EDIT_FAMILY.formatted(memberId, valid.name, maidId),
                Strings.UC8_NOTIFY_EDIT_FAMILY.formatted(valid.name))) {
            throw new ValidationException(Strings.UC8_ERR_RECORD_GONE);
        }
    }

    public void deleteFamilyMember(int maidId, FamilyMember member) {
        Officer officer = requireWelfareOfficer();
        Maid maid = requireMaid(maidId);
        if (!service.deleteFamilyMember(officer, maid, member.getFamilyMemberId(),
                Strings.UC8_AUDIT_DELETE_FAMILY.formatted(member.getFamilyMemberId(), member.getName(), maidId),
                Strings.UC8_NOTIFY_DELETE_FAMILY.formatted(member.getName()))) {
            throw new ValidationException(Strings.UC8_ERR_RECORD_GONE);
        }
    }

    public void addEmergencyContact(int maidId, String name, String relation, String phone) {
        Officer officer = requireWelfareOfficer();
        String validName = required(name, MAX_NAME, Strings.UC8_ERR_NAME);
        String validRelation = required(relation, MAX_RELATION, Strings.UC8_ERR_RELATION);
        String validPhone = phone(phone, true);
        Maid maid = requireMaid(maidId);
        service.addEmergencyContact(officer, maid, validName, validRelation, validPhone,
                Strings.UC8_AUDIT_ADD_CONTACT.formatted(validName, validRelation, maidId),
                Strings.UC8_NOTIFY_ADD_CONTACT.formatted(validName, validRelation));
    }

    public void deleteEmergencyContact(int maidId, EmergencyContact contact) {
        Officer officer = requireWelfareOfficer();
        Maid maid = requireMaid(maidId);
        if (!service.deleteEmergencyContact(officer, maid, contact.getContactId(),
                Strings.UC8_AUDIT_DELETE_CONTACT.formatted(contact.getContactId(), contact.getName(), maidId),
                Strings.UC8_NOTIFY_DELETE_CONTACT.formatted(contact.getName()))) {
            throw new ValidationException(Strings.UC8_ERR_RECORD_GONE);
        }
    }

    // Checks

    private record Valid(String name, String relation, int age, String idNumber, String school, String contact) {
    }

    private Valid check(FamilyMemberForm form) {
        String name = required(form.name(), MAX_NAME, Strings.UC8_ERR_NAME);
        String relation = required(form.relation(), MAX_RELATION, Strings.UC8_ERR_RELATION);
        String ageText = toAsciiDigits(form.age() == null ? "" : form.age().trim());
        if (!ageText.matches("[0-9]{1,3}") || Integer.parseInt(ageText) > 120) {
            throw new ValidationException(Strings.UC8_ERR_AGE);
        }
        String idNumber = blankToNull(toAsciiDigits(form.cnicOrBForm() == null ? "" : form.cnicOrBForm().trim()));
        if (idNumber != null && !idNumber.matches("[0-9]{1," + MAX_ID_NUMBER + "}")) {
            throw new ValidationException(Strings.UC8_ERR_ID_NUMBER);
        }
        String school = blankToNull(form.school() == null ? "" : form.school().trim());
        if (school != null && school.length() > MAX_SCHOOL) {
            throw new ValidationException(Strings.UC8_ERR_SCHOOL);
        }
        return new Valid(name, relation, Integer.parseInt(ageText), idNumber, school,
                phone(form.contactNumber(), false));
    }

    /** 11 digits starting with 03; returns null for an optional blank. */
    private static String phone(String typed, boolean mandatory) {
        String text = toAsciiDigits(typed == null ? "" : typed.trim());
        if (text.isEmpty() && !mandatory) {
            return null;
        }
        if (!text.matches("03[0-9]{9}")) {
            throw new ValidationException(Strings.UC8_ERR_PHONE);
        }
        return text;
    }

    private static String required(String typed, int maxLength, String message) {
        String text = typed == null ? "" : typed.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new ValidationException(message);
        }
        return text;
    }

    private static String blankToNull(String text) {
        return text.isEmpty() ? null : text;
    }

    private Maid requireMaid(int maidId) {
        return service.findMaid(maidId).orElseThrow(() -> new ValidationException(Strings.UC8_ERR_MAID_GONE));
    }

    private static Officer requireWelfareOfficer() {
        if (Session.isLoggedIn()
                && Session.getUser() instanceof Officer officer
                && officer.getPermissions().contains(Permission.MANAGE_FAMILY_RECORDS)) {
            return officer;
        }
        throw new ValidationException(Strings.UC8_ERR_NOT_ALLOWED);
    }

    /** Urdu keyboards can type Urdu digits; they are stored as 0-9. */
    private static String toAsciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            result.append(Character.isDigit(c) ? (char) ('0' + Character.digit(c, 10)) : c);
        }
        return result.toString();
    }
}
