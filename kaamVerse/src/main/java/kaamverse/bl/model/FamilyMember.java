package kaamverse.bl.model;

/** A member of a maid's family. */
public class FamilyMember {

    private int familyMemberId;
    private int maidId;
    private String name;
    private String relation;
    private int age;
    private String cnicOrBForm;
    private boolean isDependent;
    private String school;
    private String contactNumber;

    public int getFamilyMemberId() {
        return familyMemberId;
    }

    public void setFamilyMemberId(int familyMemberId) {
        this.familyMemberId = familyMemberId;
    }

    public int getMaidId() {
        return maidId;
    }

    public void setMaidId(int maidId) {
        this.maidId = maidId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCnicOrBForm() {
        return cnicOrBForm;
    }

    public void setCnicOrBForm(String cnicOrBForm) {
        this.cnicOrBForm = cnicOrBForm;
    }

    public boolean isDependent() {
        return isDependent;
    }

    public void setDependent(boolean isDependent) {
        this.isDependent = isDependent;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
