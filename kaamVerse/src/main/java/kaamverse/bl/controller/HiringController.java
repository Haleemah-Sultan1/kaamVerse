package kaamverse.bl.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kaamverse.bl.model.Hire;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Locality;
import kaamverse.bl.model.MaidSearchFilter;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.Street;
import kaamverse.bl.service.HiringService;
import kaamverse.bl.service.HiringService.HireRow;
import kaamverse.bl.service.HiringService.HireSummary;
import kaamverse.bl.service.HiringService.MaidListing;
import kaamverse.bl.service.HiringService.MaidProfile;
import kaamverse.bl.service.HiringService.ServiceOffer;
import kaamverse.bl.service.Session;
import kaamverse.common.HireStatus;
import kaamverse.common.StringsB;
import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

// Controller for UC3. Nothing is saved until confirmHireRequest().
public class HiringController {

    private static final double MAX_QUANTITY = 1000;
    private static final double MAX_WAGE = 99_999_999.99;
    private static final int MAX_HOURS_TEXT = 100;

    private final HiringService service = new HiringService();
    private HireSummary draft;

    public List<Locality> getLocalities() {
        requireWoman();
        return service.localities();
    }

    public List<Street> getStreets(int localityId) {
        requireWoman();
        return service.streets(localityId);
    }

    public List<Service> getServices() {
        requireWoman();
        return service.services();
    }

    public List<MaidListing> searchMaids(Integer localityId, Integer streetId, Integer serviceId,
                                         String minRatingText, WeekDay day, TimeSlot timeSlot) {
        requireWoman();
        double minRating = parseRating(minRatingText);
        return service.search(new MaidSearchFilter(localityId, streetId, serviceId, minRating, day, timeSlot));
    }

    public MaidProfile openMaidProfile(int maidId) {
        requireWoman();
        return service.profile(maidId).orElseThrow(() -> new ValidationException(StringsB.UC3_ERR_MAID_GONE));
    }

    // quantityTexts: serviceId -> text typed for each ticked service
    public HireSummary sendHireRequest(int maidId, Map<Integer, String> quantityTexts, String hoursText) {
        HiringWoman woman = requireWoman();
        draft = null;
        checkCanHire(woman, maidId);
        MaidProfile profile = openMaidProfile(maidId);
        String hours = hoursText == null ? "" : hoursText.trim();
        if (hours.isEmpty() || hours.length() > MAX_HOURS_TEXT) {
            throw new ValidationException(StringsB.UC3_ERR_HOURS);
        }
        Map<Integer, Double> quantities = new LinkedHashMap<>();
        if (quantityTexts != null) {
            for (ServiceOffer offer : profile.offers()) {
                String text = quantityTexts.get(offer.service().getServiceId());
                if (text == null) {
                    continue;
                }
                String name = offer.service().getName();
                if (offer.band() == null) {
                    throw new ValidationException(StringsB.UC3_ERR_NO_BAND.formatted(name));
                }
                quantities.put(offer.service().getServiceId(), parseQuantity(text, name));
            }
        }
        if (quantities.isEmpty()) {
            throw new ValidationException(StringsB.UC3_ERR_NO_SERVICE);
        }
        HireSummary summary;
        try {
            summary = service.buildHire(woman.getHiringWomanId(), profile, quantities, hours);
        } catch (IllegalStateException e) {
            throw new ValidationException(StringsB.UC3_ERR_PRICE);
        }
        if (summary.hire().getCalculatedWage() > MAX_WAGE) {
            throw new ValidationException(StringsB.UC3_ERR_TOTAL);
        }
        draft = summary;
        return summary;
    }

    public Hire confirmHireRequest() {
        HiringWoman woman = requireWoman();
        if (draft == null) {
            throw new ValidationException(StringsB.UC3_ERR_NO_DRAFT);
        }
        Hire hire = draft.hire();
        checkCanHire(woman, hire.getMaidId());
        service.saveHire(hire);
        draft = null;
        return hire;
    }

    public List<HireRow> getMyHires() {
        return service.hiresOf(requireWoman().getHiringWomanId());
    }

    public void activateHire(int hireId) {
        HiringWoman woman = requireWoman();
        Hire hire = service.findOwnedHire(woman.getHiringWomanId(), hireId)
                .orElseThrow(() -> new ValidationException(StringsB.UC3_ERR_HIRE_GONE));
        if (hire.getStatus() != HireStatus.Pending) {
            throw new ValidationException(StringsB.UC3_ERR_NOT_PENDING);
        }
        service.markActive(hire);
    }

    public void endHire(int hireId) {
        HiringWoman woman = requireWoman();
        Hire hire = service.findOwnedHire(woman.getHiringWomanId(), hireId)
                .orElseThrow(() -> new ValidationException(StringsB.UC3_ERR_HIRE_GONE));
        if (hire.getStatus() != HireStatus.Active) {
            throw new ValidationException(StringsB.UC3_ERR_NOT_ACTIVE);
        }
        service.markEnded(hire);
    }

    // read again from the database, because UC12 can suspend her after login
    private void checkCanHire(HiringWoman woman, int maidId) {
        if (service.isSuspended(woman.getHiringWomanId())) {
            throw new ValidationException(StringsB.UC3_ERR_SUSPENDED);
        }
        if (service.hasOpenHire(woman.getHiringWomanId(), maidId)) {
            throw new ValidationException(StringsB.UC3_ERR_OPEN_HIRE);
        }
    }

    private double parseRating(String text) {
        String cleaned = text == null ? "" : text.trim();
        if (cleaned.isEmpty()) {
            return 0;
        }
        double rating;
        try {
            rating = Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new ValidationException(StringsB.UC3_ERR_RATING);
        }
        if (!Double.isFinite(rating) || rating < 0 || rating > 5) {
            throw new ValidationException(StringsB.UC3_ERR_RATING);
        }
        return rating;
    }

    private double parseQuantity(String text, String serviceName) {
        double quantity;
        try {
            quantity = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(StringsB.UC3_ERR_QTY.formatted(serviceName));
        }
        if (!Double.isFinite(quantity) || quantity <= 0 || quantity > MAX_QUANTITY) {
            throw new ValidationException(StringsB.UC3_ERR_QTY.formatted(serviceName));
        }
        return Math.round(quantity * 100) / 100.0;
    }

    private HiringWoman requireWoman() {
        if (Session.isLoggedIn() && Session.getUser() instanceof HiringWoman woman) {
            return woman;
        }
        throw new ValidationException(StringsB.UC3_ERR_NOT_ALLOWED);
    }
}