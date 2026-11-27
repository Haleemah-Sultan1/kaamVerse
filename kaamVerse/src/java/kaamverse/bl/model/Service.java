package kaamverse.bl.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kaamverse.common.WageUnit;

/** A kind of work. GRASP Creator and Information Expert for its wage bands. */
public class Service {

    private int serviceId;
    private String name;
    private List<WageBand> wageBands = new ArrayList<>();

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WageBand> getWageBands() {
        return wageBands;
    }

    public void setWageBands(List<WageBand> wageBands) {
        this.wageBands = wageBands;
    }

    /** GRASP Creator: a Service creates its own WageBand. */
    public WageBand addBand(double minRate, double maxRate, WageUnit unit, LocalDate effectiveDate) {
        WageBand band = new WageBand();
        band.setServiceId(serviceId);
        band.setMinRate(minRate);
        band.setMaxRate(maxRate);
        band.setUnit(unit);
        band.setEffectiveDate(effectiveDate);
        wageBands.add(band);
        return band;
    }

    /**
     * GRASP Information Expert: the newest band that is not archived and is
     * already in effect, or null if there is none.
     */
    public WageBand getCurrentBand() {
        LocalDate today = LocalDate.now();
        return wageBands.stream()
                .filter(band -> !band.isArchived() && !band.getEffectiveDate().isAfter(today))
                .max(Comparator.comparing(WageBand::getEffectiveDate).thenComparingInt(WageBand::getBandId))
                .orElse(null);
    }
}
