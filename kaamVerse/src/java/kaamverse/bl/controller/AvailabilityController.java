package kaamverse.bl.controller;

import java.util.Collection;
import java.util.List;

import kaamverse.bl.model.AvailabilitySlot;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.SlotChoice;
import kaamverse.bl.service.AvailabilityService;
import kaamverse.bl.service.Session;
import kaamverse.common.Strings;

/**
 * GRASP Controller for UC7 Manage Availability Schedule. It checks that the
 * user is a maid itself, because the menu only hides the button.
 * Pattern: getAvailability, saveAvailability.
 */
public class AvailabilityController {

    private final AvailabilityService service = new AvailabilityService();

    /** The logged-in maid's saved slots, with their booked flags. */
    public List<AvailabilitySlot> getAvailability() {
        return service.load(requireMaid());
    }

    /** Replaces her schedule with the ticked slots; returns the saved schedule. */
    public List<AvailabilitySlot> saveAvailability(Collection<SlotChoice> selected) {
        return service.save(requireMaid(), selected);
    }

    private static Maid requireMaid() {
        if (Session.isLoggedIn() && Session.getUser() instanceof Maid maid) {
            return maid;
        }
        throw new ValidationException(Strings.UC7_ERR_NOT_ALLOWED);
    }
}
