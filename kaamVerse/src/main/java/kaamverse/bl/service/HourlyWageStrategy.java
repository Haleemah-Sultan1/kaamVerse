package kaamverse.bl.service;

import kaamverse.bl.model.WageBand;

/** Quantity is the number of hours. */
public class HourlyWageStrategy implements WageStrategy {

    @Override
    public double calculate(WageBand band, double hours) {
        return band.getMaxRate() * hours;
    }
}
