package kaamverse.bl.controller;

/** UC8: what the officer typed for a family member, before it is checked. */
public record FamilyMemberForm(String name, String relation, String age, String cnicOrBForm,
                               boolean dependent, String school, String contactNumber) {
}
