package kaamverse.bl.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import kaamverse.bl.model.Complaint;
import kaamverse.bl.model.Hire;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.common.ComplaintStatus;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.HireStatus;
import kaamverse.common.ProblemType;
import kaamverse.common.StringsB;
import kaamverse.db.dao.ComplaintDao;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.DaoFactoryB;
import kaamverse.db.dao.HireDao;
import kaamverse.db.dao.HiringWomanDao;
import kaamverse.db.dao.MaidDao;

public class ComplaintService {

    public record EmployerHire(HiringWoman employer, Hire hire) {
    }

    private static final String REFERENCE_PREFIX = "CMP-";
    private static final int MAX_SAVE_ATTEMPTS = 5;
    private static final Path FILE_DIR = Path.of("complaint_files");

    private final HireDao hireDao = DaoFactoryB.hireDao();
    private final ComplaintDao complaintDao = DaoFactoryB.complaintDao();
    private final MaidDao maidDao = DaoFactory.maidDao();
    private final HiringWomanDao womanDao = DaoFactory.hiringWomanDao();

    public Optional<Maid> findMaid(int maidId) {
        return maidDao.findById(maidId);
    }

    // one entry per employer: the maid's newest Active or Ended hire with her
    public List<EmployerHire> employerHires(int maidId) {
        Map<Integer, EmployerHire> latest = new LinkedHashMap<>();
        for (Hire hire : hireDao.findByMaid(maidId)) {
            boolean worked = hire.getStatus() == HireStatus.Active || hire.getStatus() == HireStatus.Ended;
            if (!worked || latest.containsKey(hire.getHiringWomanId())) {
                continue;
            }
            womanDao.findById(hire.getHiringWomanId())
                    .ifPresent(woman -> latest.put(woman.getHiringWomanId(), new EmployerHire(woman, hire)));
        }
        return new ArrayList<>(latest.values());
    }

    // saves the complaint as Filed; the employer is never told
    public Complaint submit(Maid maid, Hire hire, ProblemType type, String transcript, Path audio,
                            Path evidence, LocalDate incidentDate) {
        DuplicateRecordException clash = null;
        for (int attempt = 0; attempt < MAX_SAVE_ATTEMPTS; attempt++) {
            Complaint complaint = new Complaint();
            complaint.setReferenceNo(nextReferenceNo(LocalDate.now().getYear()));
            complaint.setMaidId(maid.getMaidId());
            complaint.setHireId(hire.getHireId());
            complaint.setProblemType(type.getLabel());
            complaint.setUrgency(type.getUrgency());
            complaint.setTranscript(transcript);
            complaint.setAudioPath(keep(audio, complaint.getReferenceNo() + "-audio"));
            complaint.setEvidencePath(keep(evidence, complaint.getReferenceNo() + "-evidence"));
            complaint.setIncidentDate(incidentDate);
            complaint.setStatus(ComplaintStatus.Filed);
            complaint.setFiledDate(LocalDate.now());
            try {
                complaintDao.insert(complaint);
                return complaint;
            } catch (DuplicateRecordException e) {
                clash = e; // another complaint took this number; pick the next one
            }
        }
        throw new DatabaseException("Could not find a free reference number", clash);
    }

    private String nextReferenceNo(int year) {
        String prefix = REFERENCE_PREFIX + year;
        int next = 1;
        Optional<String> latest = complaintDao.findLatestReferenceNo(prefix);
        if (latest.isPresent()) {
            try {
                next = Integer.parseInt(latest.get().substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                throw new DatabaseException("Unexpected reference number format: " + latest.get(), e);
            }
        }
        return prefix + String.format(Locale.ROOT, "%04d", next);
    }

    // copies the file into complaint_files and returns the saved path
    private String keep(Path source, String name) {
        if (source == null) {
            return null;
        }
        String file = source.getFileName().toString();
        int dot = file.lastIndexOf('.');
        String extension = dot < 0 ? "" : file.substring(dot).toLowerCase(Locale.ROOT);
        Path target = FILE_DIR.resolve(name + extension);
        try {
            Files.createDirectories(FILE_DIR);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(StringsB.UC11_ERR_FILE_SAVE);
        }
        return target.toString();
    }
}