package kaamverse.bl.model;

import java.time.LocalDate;

import kaamverse.common.WageUnit;

/** Allowed rate range for a service. */
public class WageBand {

    private int bandId;
    private int serviceId;
    private double minRate;
    private double maxRate;
    private WageUnit unit;
    private LocalDate effectiveDate;
    private boolean isArchived;

    public int getBandId() {
        return bandId;
    }

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public double getMinRate() {
        return minRate;
    }

    public void setMinRate(double minRate) {
        this.minRate = minRate;
    }

    public double getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(double maxRate) {
        this.maxRate = maxRate;
    }

    public WageUnit getUnit() {
        return unit;
    }

    public void setUnit(WageUnit unit) {
        this.unit = unit;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public void archive() {
        isArchived = true;
    }
}
