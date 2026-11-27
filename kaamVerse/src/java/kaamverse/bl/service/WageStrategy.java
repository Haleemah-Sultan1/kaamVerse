package kaamverse.bl.service;

import kaamverse.bl.model.WageBand;

/** GoF Strategy: how a wage is worked out for one unit. */
public interface WageStrategy {

    double calculate(WageBand band, double quantity);
}
