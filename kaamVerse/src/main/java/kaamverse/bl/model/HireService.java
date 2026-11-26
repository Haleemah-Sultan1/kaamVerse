package kaamverse.bl.model;

import kaamverse.common.WageUnit;

/** One service inside a hire. 'service' is loaded for pricing and is not a column. */
public class HireService {

    private int hireId;
    private int serviceId;
    private double quantity;
    private WageUnit unit;
    private double wage;
    private Service service;

    public int getHireId() {
        return hireId;
    }

    public void setHireId(int hireId) {
        this.hireId = hireId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public WageUnit getUnit() {
        return unit;
    }

    public void setUnit(WageUnit unit) {
        this.unit = unit;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
