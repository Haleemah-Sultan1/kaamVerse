package kaamverse.bl.model;

import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

/** UC7: one day and part of the day ticked on the schedule grid. */
public record SlotChoice(WeekDay day, TimeSlot timeSlot) {
}
