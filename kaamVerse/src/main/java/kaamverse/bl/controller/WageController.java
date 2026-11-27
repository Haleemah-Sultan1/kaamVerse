package kaamverse.bl.controller;

import java.time.LocalDate;
import java.util.List;

import kaamverse.bl.model.Officer;
import kaamverse.bl.model.Service;
import kaamverse.bl.service.Session;
import kaamverse.bl.service.WageService;
import kaamverse.common.Permission;
import kaamverse.common.StringsB;
import kaamverse.common.WageUnit;

/**
 * GRASP Controller for UC6 Set Standard Wage Bands. The screen talks only to
 * this class. It checks the officer's permission on every call, because the
 * menu only hides the button, and it validates every input.
 */
public class WageController {

    private static final double MAX_RATE = 1_000_000;

    private final WageService service = new WageService();

    public List<WageService.BandRow> getCurrentBands() {
        requireWageOfficer();
        return service.currentAndScheduledBands();
    }

    public List<Service> getServices() {
        requireWageOfficer();
        return service.services();
    }

    /** Validates the form and saves the new band. Throws ValidationException for bad input. */
    public void submitWageBand(Service serviceType, String minText, String maxText,
                               WageUnit unit, LocalDate effectiveDate) {
        Officer officer = requireWageOfficer();
        if (serviceType == null) {
            throw new ValidationException(StringsB.UC6_ERR_SERVICE);
        }
        if (unit == null) {
            throw new ValidationException(StringsB.UC6_ERR_UNIT);
        }
        double min = parseRate(minText, StringsB.UC6_ERR_MIN);
        double max = parseRate(maxText, StringsB.UC6_ERR_MAX);
        if (min >= max) {
            throw new ValidationException(StringsB.UC6_ERR_RANGE);
        }
        if (effectiveDate == null || effectiveDate.isBefore(LocalDate.now())) {
            throw new ValidationException(StringsB.UC6_ERR_DATE);
        }
        service.setBand(officer, serviceType, min, max, unit, effectiveDate);
    }

    /** A positive rate with at most two decimals. */
    private double parseRate(String text, String errorMessage) {
        String cleaned = text == null ? "" : text.trim().replace(",", "");
        double rate;
        try {
            rate = Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new ValidationException(errorMessage);
        }
        if (!Double.isFinite(rate) || rate <= 0) {
            throw new ValidationException(errorMessage);
        }
        if (rate > MAX_RATE) {
            throw new ValidationException(StringsB.UC6_ERR_TOO_HIGH);
        }
        return Math.round(rate * 100) / 100.0;
    }

    private Officer requireWageOfficer() {
        if (Session.isLoggedIn()
                && Session.getUser() instanceof Officer officer
                && officer.getPermissions().contains(Permission.MANAGE_WAGE_BANDS)) {
            return officer;
        }
        throw new ValidationException(StringsB.UC6_ERR_NOT_ALLOWED);
    }
}