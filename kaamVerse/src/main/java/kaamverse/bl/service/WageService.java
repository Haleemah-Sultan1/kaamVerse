package kaamverse.bl.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kaamverse.bl.model.AuditLogEntry;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.WageBand;
import kaamverse.common.StringsB;
import kaamverse.common.WageUnit;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.DaoFactoryB;
import kaamverse.db.dao.ServiceDao;
import kaamverse.db.dao.TransactionManager;
import kaamverse.db.dao.WageBandDao;

/** UC6 data access and saving. GRASP High Cohesion: wage bands only. */
public class WageService {

    /** Length of the action column in audit_log_entry. */
    private static final int MAX_ACTION_LENGTH = 255;

    private final ServiceDao serviceDao = DaoFactory.serviceDao();
    private final WageBandDao wageBandDao = DaoFactoryB.wageBandDao();

    /** One line of the wage table. 'current' is false for a band that starts later. */
    public record BandRow(Service service, WageBand band, boolean current) {
    }

    public List<Service> services() {
        return serviceDao.findAll();
    }

    /** The bands in force now, plus the ones scheduled for a later date. */
    public List<BandRow> currentAndScheduledBands() {
        List<WageBand> notArchived = wageBandDao.findNotArchived();
        LocalDate today = LocalDate.now();
        List<BandRow> rows = new ArrayList<>();
        for (Service service : serviceDao.findAll()) {
            List<WageBand> bands = new ArrayList<>();
            for (WageBand band : notArchived) {
                if (band.getServiceId() == service.getServiceId()) {
                    bands.add(band);
                }
            }
            bands.sort(Comparator.comparing(WageBand::getEffectiveDate));
            service.setWageBands(bands);
            WageBand current = service.getCurrentBand(); // GRASP Information Expert
            for (WageBand band : bands) {
                boolean isCurrent = current != null && band.getBandId() == current.getBandId();
                if (isCurrent || band.getEffectiveDate().isAfter(today)) {
                    rows.add(new BandRow(service, band, isCurrent));
                }
            }
        }
        return rows;
    }

    /**
     * Sets a new band for a service. If it takes effect today, the bands it
     * replaces are archived. The archive, the new band and the audit entry are
     * saved together, or not at all. A band that starts later is only added;
     * the old band stays in force until then, so the service is never left
     * without a wage band.
     */
    public WageBand setBand(Officer officer, Service serviceType, double minRate, double maxRate,
                            WageUnit unit, LocalDate effectiveDate) {
        return TransactionManager.call(() -> {
            serviceType.setWageBands(new ArrayList<>(wageBandDao.findByService(serviceType.getServiceId())));
            if (!effectiveDate.isAfter(LocalDate.now())) {
                for (WageBand old : serviceType.getWageBands()) {
                    if (!old.isArchived() && !old.getEffectiveDate().isAfter(effectiveDate)) {
                        old.archive();
                        wageBandDao.archive(old.getBandId());
                    }
                }
            }
            // GRASP Creator: the Service creates its own WageBand.
            WageBand band = serviceType.addBand(minRate, maxRate, unit, effectiveDate);
            wageBandDao.insert(band);
            AuditLogEntry.log(officer, truncate(StringsB.UC6_AUDIT_SET.formatted(
                    serviceType.getName(), minRate, maxRate, StringsB.unitName(unit), effectiveDate)));
            return band;
        });
    }

    private static String truncate(String action) {
        return action.length() <= MAX_ACTION_LENGTH
                ? action
                : action.substring(0, MAX_ACTION_LENGTH - 3) + "...";
    }
}