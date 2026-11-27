package kaamverse.bl.service;

import kaamverse.bl.model.WageBand;

/** Quantity is the number of days. */
public class DailyWageStrategy implements WageStrategy {

    @Override
    public double calculate(WageBand band, double days) {
        return band.getMaxRate() * days;
    }
}
