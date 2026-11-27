package kaamverse.bl.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import kaamverse.bl.model.Locality;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.Street;
import kaamverse.bl.model.VerificationRequest;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.LocalityDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.ServiceDao;
import kaamverse.db.dao.StreetDao;
import kaamverse.db.dao.TransactionManager;
import kaamverse.db.dao.VerificationRequestDao;

/** UC1 data access and saving. GRASP High Cohesion: registration only. */
public class RegistrationService {

    private final MaidDao maidDao = DaoFactory.maidDao();
    private final LocalityDao localityDao = DaoFactory.localityDao();
    private final StreetDao streetDao = DaoFactory.streetDao();
    private final ServiceDao serviceDao = DaoFactory.serviceDao();
    private final VerificationRequestDao requestDao = DaoFactory.verificationRequestDao();

    public List<Locality> localities() {
        return localityDao.findAll();
    }

    public List<Street> streets(int localityId) {
        return streetDao.findByLocality(localityId);
    }

    public List<Service> services() {
        return serviceDao.findAll();
    }

    public boolean isCnicRegistered(String cnic) {
        return maidDao.findByCnic(cnic).isPresent();
    }

    public boolean isPhoneRegistered(String phone) {
        return maidDao.findByPhone(phone).isPresent();
    }

    /**
     * Saves the maid, her services and her verification request together.
     * The CNIC image is copied into the user's home folder, outside the
     * project, so that identity cards are never committed to Git.
     */
    public void register(Maid maid, Path cnicImage) {
        Path stored = copyImage(cnicImage);
        maid.setCnicImagePath(stored.toString());
        boolean saved = false;
        try {
            TransactionManager.run(() -> {
                int maidId = maidDao.insert(maid);
                maid.getServices().forEach(service -> service.setMaidId(maidId));
                maidDao.saveServices(maidId, maid.getServices());
                VerificationRequest request = maid.createVerificationRequest();
                requestDao.insert(request);
            });
            saved = true;
        } finally {
            if (!saved) {
                delete(stored);
            }
        }
    }

    private Path copyImage(Path source) {
        String fileName = source.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        try {
            Path folder = Path.of(System.getProperty("user.home"), ".kaamverse", "cnic");
            Files.createDirectories(folder);
            return Files.copy(source, folder.resolve(UUID.randomUUID() + extension));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not copy the CNIC image", e);
        }
    }

    private void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Could not delete " + file + ": " + e.getMessage());
        }
    }
}
