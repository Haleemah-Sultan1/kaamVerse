package kaamverse.bl.service;

import java.util.Collection;
import java.util.List;

import kaamverse.bl.model.AvailabilitySlot;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.SlotChoice;
import kaamverse.db.dao.AvailabilitySlotDao;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.TransactionManager;

/** UC7 data access and saving. GRASP High Cohesion: the weekly schedule only. */
public class AvailabilityService {

    private final AvailabilitySlotDao slotDao = DaoFactory.availabilitySlotDao();

    /** Her saved slots, as they are in the database now. */
    public List<AvailabilitySlot> load(Maid maid) {
        return slotDao.findByMaid(maid.getMaidId());
    }

    /**
     * Deletes her old slots and saves the chosen ones in one transaction.
     * Her slots are read again inside it, so a slot booked in the meantime
     * is kept (still booked) even if it was not ticked.
     */
    public List<AvailabilitySlot> save(Maid maid, Collection<SlotChoice> chosen) {
        return TransactionManager.call(() -> {
            maid.setAvailability(slotDao.findByMaid(maid.getMaidId()));
            maid.replaceAvailability(chosen); // GRASP Creator: the Maid creates the slots
            slotDao.deleteByMaid(maid.getMaidId());
            slotDao.insertAll(maid.getAvailability());
            return slotDao.findByMaid(maid.getMaidId());
        });
    }
}
