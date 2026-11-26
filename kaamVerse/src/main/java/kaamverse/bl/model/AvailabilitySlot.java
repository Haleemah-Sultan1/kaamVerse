package kaamverse.bl.model;

import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

/** One slot in a maid's week. */
public class AvailabilitySlot {

    private int slotId;
    private int maidId;
    private WeekDay day;
    private TimeSlot timeSlot;
    private boolean isBooked;

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getMaidId() {
        return maidId;
    }

    public void setMaidId(int maidId) {
        this.maidId = maidId;
    }

    public WeekDay getDay() {
        return day;
    }

    public void setDay(WeekDay day) {
        this.day = day;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean isBooked) {
        this.isBooked = isBooked;
    }
}
