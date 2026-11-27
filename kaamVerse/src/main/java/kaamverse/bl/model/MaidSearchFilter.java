package kaamverse.bl.model;

import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

public record MaidSearchFilter(Integer localityId, Integer streetId, Integer serviceId,
                               double minRating, WeekDay day, TimeSlot timeSlot) {
}