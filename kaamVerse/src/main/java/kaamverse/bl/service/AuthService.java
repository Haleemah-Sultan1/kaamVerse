package kaamverse.bl.service;

import java.util.Optional;

import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.User;
import kaamverse.common.HashUtil;
import kaamverse.common.UserRole;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.HiringWomanDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.OfficerDao;

/** Checks credentials. GRASP High Cohesion: authentication only. */
public class AuthService {

    private final MaidDao maidDao = DaoFactory.maidDao();
    private final HiringWomanDao hiringWomanDao = DaoFactory.hiringWomanDao();
    private final OfficerDao officerDao = DaoFactory.officerDao();

    /**
     * @param identifier phone for maids and hiring women, username for officers
     * @param secret     PIN or password, not yet hashed
     * @return the user, or empty if the credentials are wrong
     */
    public Optional<User> authenticate(UserRole role, String identifier, String secret) {
        String hash = HashUtil.sha256(secret);
        return switch (role) {
            case MAID -> maidDao.findByPhone(identifier)
                    .filter((Maid maid) -> hash.equals(maid.getPin()))
                    .map(maid -> (User) maid);
            case HIRING_WOMAN -> hiringWomanDao.findByPhone(identifier)
                    .filter((HiringWoman woman) -> hash.equals(woman.getPin()))
                    .map(woman -> (User) woman);
            case OFFICER -> officerDao.findByUsername(identifier)
                    .filter((Officer officer) -> hash.equals(officer.getPasswordHash()))
                    .map(officer -> (User) officer);
        };
    }
}
