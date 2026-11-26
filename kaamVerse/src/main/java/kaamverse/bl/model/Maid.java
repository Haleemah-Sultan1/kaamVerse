package kaamverse.bl.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kaamverse.common.FundingStatus;
import kaamverse.common.MaidStatus;
import kaamverse.common.ReviewStatus;
import kaamverse.common.TimeSlot;
import kaamverse.common.UserRole;
import kaamverse.common.WeekDay;

/** A domestic worker. GRASP Creator and Information Expert for her own records. */
public class Maid implements User {

    private int maidId;
    private String name;
    private String fatherName;
    private String cnic;
    private String phone;
    private String address;
    private String pin;
    private String cnicImagePath;
    private MaidStatus status;
    private double averageRating;
    private int streetId;
    private LocalDate registeredDate;
    private List<MaidService> services = new ArrayList<>();
    private List<AvailabilitySlot> availability = new ArrayList<>();
    private List<FamilyMember> familyMembers = new ArrayList<>();
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

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

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCnicImagePath() {
        return cnicImagePath;
    }

    public void setCnicImagePath(String cnicImagePath) {
        this.cnicImagePath = cnicImagePath;
    }

    public MaidStatus getStatus() {
        return status;
    }

    public void setStatus(MaidStatus status) {
        this.status = status;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getStreetId() {
        return streetId;
    }

    public void setStreetId(int streetId) {
        this.streetId = streetId;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public List<MaidService> getServices() {
        return services;
    }

    public void setServices(List<MaidService> services) {
        this.services = services;
    }

    public List<AvailabilitySlot> getAvailability() {
        return availability;
    }

    public void setAvailability(List<AvailabilitySlot> availability) {
        this.availability = availability;
    }

    public List<FamilyMember> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<FamilyMember> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public List<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    @Override
    public int getId() {
        return maidId;
    }

    @Override
    public UserRole getUserRole() {
        return UserRole.MAID;
    }

    /** GRASP Creator: replaces the weekly schedule with the selected slots. */
    public void setAvailabilitySlots(List<AvailabilitySlot> selected) {
        List<AvailabilitySlot> previous = new ArrayList<>(availability);
        List<AvailabilitySlot> chosen = new ArrayList<>(selected);
        availability.clear();
        for (AvailabilitySlot slot : chosen) {
            AvailabilitySlot added = addSlot(slot.getDay(), slot.getTimeSlot());
            for (AvailabilitySlot old : previous) {
                if (old.getDay() == added.getDay() && old.getTimeSlot() == added.getTimeSlot()) {
                    added.setSlotId(old.getSlotId());
                    added.setBooked(old.isBooked());
                }
            }
        }
    }

    public AvailabilitySlot addSlot(WeekDay day, TimeSlot timeSlot) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setMaidId(maidId);
        slot.setDay(day);
        slot.setTimeSlot(timeSlot);
        availability.add(slot);
        return slot;
    }

    /** GRASP Creator: the Maid creates her FamilyMember records. */
    public FamilyMember addFamilyMember(String memberName, String relation, int age) {
        FamilyMember member = new FamilyMember();
        member.setMaidId(maidId);
        member.setName(memberName);
        member.setRelation(relation);
        member.setAge(age);
        familyMembers.add(member);
        return member;
    }

    /** GRASP Creator: the Maid creates her EmergencyContact records. */
    public EmergencyContact addEmergencyContact(String contactName, String relation, String phoneNumber) {
        EmergencyContact contact = new EmergencyContact();
        contact.setMaidId(maidId);
        contact.setName(contactName);
        contact.setRelation(relation);
        contact.setPhoneNumber(phoneNumber);
        emergencyContacts.add(contact);
        return contact;
    }

    /** GRASP Information Expert: averages the approved reviews only. */
    public void updateAverageRating(List<Review> reviews) {
        averageRating = reviews.stream()
                .filter(review -> review.getStatus() == ReviewStatus.Approved)
                .mapToInt(Review::getOverallRating)
                .average()
                .orElse(0);
    }

    /** GRASP Creator: the Maid raises her own funding requests. */
    public FundingRequest raiseRequest(String eventType, double amountNeeded, LocalDate eventDate, String reason) {
        FundingRequest request = new FundingRequest();
        request.setMaidId(maidId);
        request.setEventType(eventType);
        request.setAmountNeeded(amountNeeded);
        request.setEventDate(eventDate);
        request.setReason(reason);
        request.setStatus(FundingStatus.Pending);
        request.setCreatedDate(LocalDate.now());
        return request;
    }
}
