package kaamverse.bl.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaamverse.bl.model.Locality;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.MaidService;
import kaamverse.bl.model.RegistrationField;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.Street;
import kaamverse.bl.service.AudioPromptService;
import kaamverse.bl.service.LoggingAudioPromptService;
import kaamverse.bl.service.RegistrationService;
import kaamverse.common.HashUtil;
import kaamverse.common.Strings;

/**
 * GRASP Controller for UC1 Register Maid. Holds the draft maid; nothing is
 * written to the database until submitRegistration().
 */
public class RegistrationController {

    private final RegistrationService service = new RegistrationService();
    private final AudioPromptService audio = new LoggingAudioPromptService();

    private Maid draft;
    private Locality locality;
    private Street street;
    private List<Service> offered = new ArrayList<>();
    /** Chosen service ID to years of experience. */
    private final Map<Integer, Integer> chosenServices = new LinkedHashMap<>();
    private File cnicImage;

    public void startNewRegistration() {
        draft = new Maid();
        locality = null;
        street = null;
        chosenServices.clear();
        cnicImage = null;
    }

    public void playPrompt(String text) {
        audio.play(text);
    }

    /** Validates one personal detail and stores it in the draft. */
    public void enterFieldValue(RegistrationField field, String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new ValidationException(field, Strings.UC1_ERR_REQUIRED.formatted(label(field)));
        }
        switch (field) {
            case NAME, FATHER_NAME -> requireMaxLength(field, text, 100);
            case ADDRESS -> requireMaxLength(field, text, 255);
            case CNIC -> text = validCnic(text);
            case PHONE -> text = validPhone(text);
            case PIN_HASH -> text = HashUtil.sha256(validPin(text));
        }
        draft.setField(field, text);
    }

    public List<Locality> getLocalities() {
        return service.localities();
    }

    /** Chooses a locality and returns its streets. */
    public List<Street> selectLocality(Locality chosen) {
        if (chosen == null) {
            throw new ValidationException(Strings.UC1_ERR_LOCALITY);
        }
        if (locality == null || locality.getLocalityId() != chosen.getLocalityId()) {
            street = null;
        }
        chosen.setStreets(service.streets(chosen.getLocalityId()));
        locality = chosen;
        return chosen.getStreets();
    }

    public void selectStreet(Street chosen) {
        if (chosen == null) {
            throw new ValidationException(Strings.UC1_ERR_STREET);
        }
        street = chosen;
    }

    public List<Service> getServices() {
        offered = service.services();
        return offered;
    }

    /** @param yearsByService chosen service ID to the years typed for it */
    public void selectServicesExperience(Map<Integer, String> yearsByService) {
        if (street == null) {
            throw new ValidationException(Strings.UC1_ERR_STREET);
        }
        if (yearsByService.isEmpty()) {
            throw new ValidationException(Strings.UC1_ERR_NO_SERVICE);
        }
        Map<Integer, Integer> parsed = new LinkedHashMap<>();
        yearsByService.forEach((serviceId, typed) -> {
            String years = toAsciiDigits(typed == null ? "" : typed.trim());
            if (!years.matches("[0-9]{1,2}")) {
                throw new ValidationException(Strings.UC1_ERR_YEARS);
            }
            parsed.put(serviceId, Integer.parseInt(years));
        });
        chosenServices.clear();
        chosenServices.putAll(parsed);

        List<MaidService> lines = new ArrayList<>();
        parsed.forEach((serviceId, years) -> {
            MaidService line = new MaidService();
            line.setServiceId(serviceId);
            line.setYears(years);
            lines.add(line);
        });
        draft.setLocationAndServices(street.getStreetId(), lines);
    }

    /** Accepts a JPEG or PNG file. */
    public void uploadCnicImage(File file) {
        String name = file == null ? "" : file.getName().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) || !file.isFile()) {
            throw new ValidationException(Strings.UC1_ERR_IMAGE);
        }
        cnicImage = file;
    }

    public void submitRegistration() {
        requireComplete();
        if (cnicImage == null || !cnicImage.isFile()) {
            throw new ValidationException(Strings.UC1_ERR_IMAGE);
        }
        // Checked again in case someone registered the same values meanwhile.
        requireUnused(RegistrationField.CNIC, draft.getCnic());
        requireUnused(RegistrationField.PHONE, draft.getPhone());
        service.register(draft, cnicImage.toPath());
    }

    // Read-back for the screen

    public Maid getDraft() {
        return draft;
    }

    public Locality getLocality() {
        return locality;
    }

    public Street getStreet() {
        return street;
    }

    public File getCnicImage() {
        return cnicImage;
    }

    /** Chosen service names with years, in the order chosen. */
    public Map<String, Integer> getChosenServices() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Service item : offered) {
            Integer years = chosenServices.get(item.getServiceId());
            if (years != null) {
                result.put(item.getName(), years);
            }
        }
        return result;
    }

    // Validation

    /** Every detail, the street and at least one service must be in the draft before saving. */
    private void requireComplete() {
        if (draft == null) {
            throw new IllegalStateException("startNewRegistration() has not been called");
        }
        for (RegistrationField field : RegistrationField.values()) {
            String value = switch (field) {
                case NAME -> draft.getName();
                case FATHER_NAME -> draft.getFatherName();
                case CNIC -> draft.getCnic();
                case PHONE -> draft.getPhone();
                case ADDRESS -> draft.getAddress();
                case PIN_HASH -> draft.getPin();
            };
            if (value == null || value.isBlank()) {
                throw new ValidationException(field, Strings.UC1_ERR_REQUIRED.formatted(label(field)));
            }
        }
        if (street == null) {
            throw new ValidationException(Strings.UC1_ERR_STREET);
        }
        if (chosenServices.isEmpty()) {
            throw new ValidationException(Strings.UC1_ERR_NO_SERVICE);
        }
    }

    /** 13 digits, dashes optional; returned as 00000-0000000-0, as stored. */
    private String validCnic(String text) {
        String entered = toAsciiDigits(text);
        if (!entered.matches("[0-9]{13}|[0-9]{5}-[0-9]{7}-[0-9]")) {
            throw new ValidationException(RegistrationField.CNIC, Strings.UC1_ERR_CNIC);
        }
        String digits = entered.replace("-", "");
        String cnic = digits.substring(0, 5) + "-" + digits.substring(5, 12) + "-" + digits.substring(12);
        requireUnused(RegistrationField.CNIC, cnic);
        return cnic;
    }

    private String validPhone(String text) {
        String phone = toAsciiDigits(text);
        if (!phone.matches("03[0-9]{9}")) {
            throw new ValidationException(RegistrationField.PHONE, Strings.UC1_ERR_PHONE);
        }
        requireUnused(RegistrationField.PHONE, phone);
        return phone;
    }

    private String validPin(String text) {
        String pin = toAsciiDigits(text);
        if (!pin.matches("[0-9]{4}")) {
            throw new ValidationException(RegistrationField.PIN_HASH, Strings.UC1_ERR_PIN);
        }
        return pin;
    }

    private void requireUnused(RegistrationField field, String value) {
        if (field == RegistrationField.CNIC && service.isCnicRegistered(value)) {
            throw new ValidationException(field, Strings.UC1_ERR_CNIC_USED);
        }
        if (field == RegistrationField.PHONE && service.isPhoneRegistered(value)) {
            throw new ValidationException(field, Strings.UC1_ERR_PHONE_USED);
        }
    }

    private void requireMaxLength(RegistrationField field, String text, int max) {
        if (text.length() > max) {
            throw new ValidationException(field, Strings.UC1_ERR_TOO_LONG.formatted(label(field)));
        }
    }

    /** Urdu keyboards can type Urdu digits; they are stored as 0-9. */
    private static String toAsciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            result.append(Character.isDigit(c) ? (char) ('0' + Character.digit(c, 10)) : c);
        }
        return result.toString();
    }

    private static String label(RegistrationField field) {
        return switch (field) {
            case NAME -> Strings.UC1_NAME;
            case FATHER_NAME -> Strings.UC1_FATHER_NAME;
            case CNIC -> Strings.UC1_CNIC;
            case PHONE -> Strings.UC1_PHONE;
            case ADDRESS -> Strings.UC1_ADDRESS;
            case PIN_HASH -> Strings.UC1_PIN;
        };
    }
}
