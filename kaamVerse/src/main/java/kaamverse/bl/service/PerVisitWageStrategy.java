package kaamverse.bl.service;

import kaamverse.bl.model.WageBand;

/** Quantity is the number of visits. */
public class PerVisitWageStrategy implements WageStrategy {

    @Override
    public double calculate(WageBand band, double visits) {
        return band.getMaxRate() * visits;
    }
}
