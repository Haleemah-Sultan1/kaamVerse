package kaamverse.bl.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.service.WageStrategy;
import kaamverse.bl.service.WageStrategyFactory;
import kaamverse.common.HireStatus;

/** A hire of a maid. GRASP Information Expert for its total wage. */
public class Hire {

    private int hireId;
    private int maidId;
    private int hiringWomanId;
    private String workingHours;
    private LocalDate requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private HireStatus status;
    private double calculatedWage;
    private List<HireService> services = new ArrayList<>();

    public int getHireId() {
        return hireId;
    }

    public void setHireId(int hireId) {
        this.hireId = hireId;
    }

    public int getMaidId() {
        return maidId;
    }

    public void setMaidId(int maidId) {
        this.maidId = maidId;
    }

    public int getHiringWomanId() {
        return hiringWomanId;
    }

    public void setHiringWomanId(int hiringWomanId) {
        this.hiringWomanId = hiringWomanId;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public HireStatus getStatus() {
        return status;
    }

    public void setStatus(HireStatus status) {
        this.status = status;
    }

    public double getCalculatedWage() {
        return calculatedWage;
    }

    public void setCalculatedWage(double calculatedWage) {
        this.calculatedWage = calculatedWage;
    }

    public List<HireService> getServices() {
        return services;
    }

    public void setServices(List<HireService> services) {
        this.services = services;
    }

    /** GRASP Creator: a Hire creates its own HireService lines. */
    public HireService addService(Service service, double quantity) {
        HireService line = new HireService();
        line.setHireId(hireId);
        line.setServiceId(service.getServiceId());
        line.setService(service);
        line.setQuantity(quantity);
        services.add(line);
        return line;
    }

    /**
     * Prices every line from its service's current band and stores the total.
     * GoF Strategy and GRASP Protected Variations: the unit chooses the
     * strategy, so Hire does not change when a new unit is added.
     * Call this when a hire is created, not when an old one is displayed,
     * so that later rate changes do not alter saved wages.
     *
     * @throws IllegalStateException if there are no services, a quantity is
     *         not positive, or a service has no current wage band
     */
    public double calculateWage() {
        if (services.isEmpty()) {
            throw new IllegalStateException("A hire needs at least one service");
        }
        double total = 0;
        for (HireService line : services) {
            Service service = line.getService();
            WageBand band = service == null ? null : service.getCurrentBand();
            if (band == null) {
                throw new IllegalStateException("No current wage band for service " + line.getServiceId());
            }
            if (line.getQuantity() <= 0) {
                throw new IllegalStateException("Quantity must be greater than zero");
            }
            WageStrategy strategy = WageStrategyFactory.forUnit(band.getUnit());
            double wage = Math.round(strategy.calculate(band, line.getQuantity()) * 100) / 100.0;
            line.setUnit(band.getUnit());
            line.setWage(wage);
            total += wage;
        }
        calculatedWage = Math.round(total * 100) / 100.0;
        return calculatedWage;
    }
}
