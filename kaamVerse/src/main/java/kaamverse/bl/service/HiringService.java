package kaamverse.bl.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import kaamverse.bl.model.AvailabilitySlot;
import kaamverse.bl.model.Hire;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Locality;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.MaidSearchFilter;
import kaamverse.bl.model.MaidService;
import kaamverse.bl.model.Review;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.Street;
import kaamverse.bl.model.WageBand;
import kaamverse.common.DatabaseException;
import kaamverse.common.HireStatus;
import kaamverse.common.MaidStatus;
import kaamverse.common.StringsB;
import kaamverse.db.dao.AvailabilitySlotDao;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.DaoFactoryB;
import kaamverse.db.dao.HireDao;
import kaamverse.db.dao.HiringWomanDao;
import kaamverse.db.dao.LocalityDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.MaidSearchDao;
import kaamverse.db.dao.ServiceDao;
import kaamverse.db.dao.StreetDao;
import kaamverse.db.dao.TransactionManager;
import kaamverse.db.dao.WageBandDao;

// UC3 data access and saving. High Cohesion: searching and hiring only.
public class HiringService {

    public record ServiceOffer(Service service, int years, WageBand band) {
    }

    public record MaidListing(Maid maid, String area, List<ServiceOffer> offers) {
    }

    public record MaidProfile(Maid maid, String area, List<ServiceOffer> offers,
                              List<AvailabilitySlot> slots, List<Review> reviews) {
    }

    public record HireSummary(Hire hire, String maidName) {
    }

    public record HireRow(Hire hire, String maidName) {
    }

    private final MaidSearchDao searchDao = DaoFactoryB.maidSearchDao();
    private final HireDao hireDao = DaoFactoryB.hireDao();
    private final WageBandDao wageBandDao = DaoFactoryB.wageBandDao();
    private final MaidDao maidDao = DaoFactory.maidDao();
    private final HiringWomanDao womanDao = DaoFactory.hiringWomanDao();
    private final ServiceDao serviceDao = DaoFactory.serviceDao();
    private final LocalityDao localityDao = DaoFactory.localityDao();
    private final StreetDao streetDao = DaoFactory.streetDao();
    private final AvailabilitySlotDao slotDao = DaoFactory.availabilitySlotDao();
    private final NotificationService notifications = new NotificationService();

    public List<Locality> localities() {
        return localityDao.findAll();
    }

    public List<Street> streets(int localityId) {
        return streetDao.findByLocality(localityId);
    }

    public List<Service> services() {
        return serviceDao.findAll();
    }

    public List<MaidListing> search(MaidSearchFilter filter) {
        Map<Integer, Service> services = servicesWithBands();
        Map<Integer, String> areas = areaNames();
        List<MaidListing> result = new ArrayList<>();
        for (Maid maid : searchDao.findMatching(filter)) {
            maid.setHasBadge(maidDao.hasTrustBadge(maid.getMaidId()));
            result.add(new MaidListing(maid, areas.getOrDefault(maid.getStreetId(), ""),
                    offers(maid.getMaidId(), services)));
        }
        return result;
    }

    // empty if the maid does not exist or is not verified
    public Optional<MaidProfile> profile(int maidId) {
        Optional<Maid> found = maidDao.findById(maidId);
        if (found.isEmpty() || found.get().getStatus() != MaidStatus.Verified) {
            return Optional.empty();
        }
        Maid maid = found.get();
        maid.setHasBadge(maidDao.hasTrustBadge(maidId));
        List<AvailabilitySlot> free = new ArrayList<>();
        for (AvailabilitySlot slot : slotDao.findByMaid(maidId)) {
            if (!slot.isBooked()) {
                free.add(slot);
            }
        }
        return Optional.of(new MaidProfile(maid, areaNames().getOrDefault(maid.getStreetId(), ""),
                offers(maidId, servicesWithBands()), free, searchDao.findApprovedReviews(maidId)));
    }

    // Creator: the Hire creates its own lines; the wage comes from the Strategy inside Hire
    public HireSummary buildHire(int womanId, MaidProfile profile, Map<Integer, Double> quantities, String hours) {
        Hire hire = new Hire();
        hire.setMaidId(profile.maid().getMaidId());
        hire.setHiringWomanId(womanId);
        hire.setWorkingHours(hours);
        hire.setRequestDate(LocalDate.now());
        hire.setStatus(HireStatus.Pending);
        for (ServiceOffer offer : profile.offers()) {
            Double quantity = quantities.get(offer.service().getServiceId());
            if (quantity != null) {
                hire.addService(offer.service(), quantity);
            }
        }
        hire.calculateWage();
        return new HireSummary(hire, profile.maid().getName());
    }

    public int saveHire(Hire hire) {
        int id = TransactionManager.call(() -> hireDao.insert(hire));
        try {
            String employer = womanDao.findById(hire.getHiringWomanId())
                    .map(HiringWoman::getName).orElse(StringsB.UC3_NOTIFY_FALLBACK);
            maidDao.findById(hire.getMaidId()).ifPresent(maid -> notifications.send(maid,
                    StringsB.UC3_NOTIFY_MAID.formatted(employer, hire.getWorkingHours())));
        } catch (DatabaseException e) {
            // the hire is already saved; a lost notification must not undo it
            System.err.println("Could not notify the maid: " + e.getMessage());
        }
        return id;
    }

    public boolean isSuspended(int womanId) {
        return womanDao.findById(womanId).map(HiringWoman::isHiringSuspended).orElse(false);
    }

    public boolean hasOpenHire(int womanId, int maidId) {
        for (Hire hire : hireDao.findByWoman(womanId)) {
            boolean open = hire.getStatus() == HireStatus.Pending || hire.getStatus() == HireStatus.Active;
            if (open && hire.getMaidId() == maidId) {
                return true;
            }
        }
        return false;
    }

    public List<HireRow> hiresOf(int womanId) {
        List<HireRow> rows = new ArrayList<>();
        for (Hire hire : hireDao.findByWoman(womanId)) {
            String name = maidDao.findById(hire.getMaidId()).map(Maid::getName).orElse("");
            rows.add(new HireRow(hire, name));
        }
        return rows;
    }

    public Optional<Hire> findOwnedHire(int womanId, int hireId) {
        return hireDao.findById(hireId).filter(hire -> hire.getHiringWomanId() == womanId);
    }

    public void markActive(Hire hire) {
        hire.setStatus(HireStatus.Active);
        hire.setStartDate(LocalDate.now());
        hireDao.updateStatus(hire.getHireId(), HireStatus.Active, hire.getStartDate(), hire.getEndDate());
    }

    public void markEnded(Hire hire) {
        hire.setStatus(HireStatus.Ended);
        hire.setEndDate(LocalDate.now());
        hireDao.updateStatus(hire.getHireId(), HireStatus.Ended, hire.getStartDate(), hire.getEndDate());
    }

    private Map<Integer, Service> servicesWithBands() {
        Map<Integer, Service> services = new LinkedHashMap<>();
        for (Service service : serviceDao.findAll()) {
            service.setWageBands(new ArrayList<>(wageBandDao.findByService(service.getServiceId())));
            services.put(service.getServiceId(), service);
        }
        return services;
    }

    private Map<Integer, String> areaNames() {
        Map<Integer, String> areas = new LinkedHashMap<>();
        for (Locality locality : localityDao.findAll()) {
            for (Street street : streetDao.findByLocality(locality.getLocalityId())) {
                areas.put(street.getStreetId(), street.getName() + ", " + locality.getName());
            }
        }
        return areas;
    }

    private List<ServiceOffer> offers(int maidId, Map<Integer, Service> services) {
        List<ServiceOffer> offers = new ArrayList<>();
        for (MaidService item : searchDao.findServices(maidId)) {
            Service service = services.get(item.getServiceId());
            if (service != null) {
                offers.add(new ServiceOffer(service, item.getYears(), service.getCurrentBand()));
            }
        }
        return offers;
    }
}