package kaamverse.bl.service;

import kaamverse.common.WageUnit;

/** GoF Factory: picks the strategy for a wage unit. */
public final class WageStrategyFactory {

    private WageStrategyFactory() {
    }

    public static WageStrategy forUnit(WageUnit unit) {
        return switch (unit) {
            case PER_HOUR -> new HourlyWageStrategy();
            case PER_DAY -> new DailyWageStrategy();
            case PER_VISIT -> new PerVisitWageStrategy();
        };
    }
}
