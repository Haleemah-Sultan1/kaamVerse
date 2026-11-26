package kaamverse.bl.model;

import java.util.ArrayList;
import java.util.List;

import kaamverse.common.HireStatus;
import kaamverse.common.UserRole;

/** An employer. Also the fund contributor and previous employer actors. */
public class HiringWoman implements User {

    private int hiringWomanId;
    private String name;
    private String phone;
    private String address;
    private String pin;
    private boolean isHiringSuspended;
    private List<Hire> hires = new ArrayList<>();

    public int getHiringWomanId() {
        return hiringWomanId;
    }

    public void setHiringWomanId(int hiringWomanId) {
        this.hiringWomanId = hiringWomanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isHiringSuspended() {
        return isHiringSuspended;
    }

    public void setHiringSuspended(boolean isHiringSuspended) {
        this.isHiringSuspended = isHiringSuspended;
    }

    public List<Hire> getHires() {
        return hires;
    }

    public void setHires(List<Hire> hires) {
        this.hires = hires;
    }

    @Override
    public int getId() {
        return hiringWomanId;
    }

    @Override
    public UserRole getUserRole() {
        return UserRole.HIRING_WOMAN;
    }

    public List<Hire> getEndedHires() {
        List<Hire> ended = new ArrayList<>();
        for (Hire hire : hires) {
            if (hire.getStatus() == HireStatus.Ended) {
                ended.add(hire);
            }
        }
        return ended;
    }
}
