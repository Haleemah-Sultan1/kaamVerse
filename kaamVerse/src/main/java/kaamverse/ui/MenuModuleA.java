package kaamverse.ui;

import java.util.List;
import java.util.Set;

import javafx.scene.Parent;
import kaamverse.common.Permission;
import kaamverse.common.Strings;
import kaamverse.common.UserRole;

/**
 * Person A's menu entries. Replace ComingSoonScreen with your own screen,
 * for example: navigator -> new MyScreen(navigator)
 */
public final class MenuModuleA {

    private static final List<MenuEntry> ENTRIES = List.of(
            // UC2
            MenuEntry.forPermission(Permission.VERIFY_MAIDS, Strings.UC2_VERIFY,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC2_VERIFY)),
            // UC7
            MenuEntry.forRole(UserRole.MAID, Strings.UC7_AVAILABILITY,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC7_AVAILABILITY)),
            // UC8
            MenuEntry.forPermission(Permission.MANAGE_FAMILY_RECORDS, Strings.UC8_FAMILY,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC8_FAMILY)),
            // UC9
            MenuEntry.forRole(UserRole.MAID, Strings.UC9_FUNDING,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC9_FUNDING)),
            // UC10
            MenuEntry.forRole(UserRole.HIRING_WOMAN, Strings.UC10_CONTRIBUTE,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC10_CONTRIBUTE)));

    private MenuModuleA() {
    }

    /** The entries this user is allowed to see. */
    public static List<MenuEntry> itemsFor(UserRole role, Set<Permission> permissions) {
        return ENTRIES.stream()
                .filter(entry -> entry.isAllowed(role, permissions))
                .toList();
    }

    /** UC1 opens from the login screen, because a new maid has no account yet. */
    public static Parent registrationScreen(SceneNavigator navigator) {
        return new ComingSoonScreen(navigator, Strings.UC1_REGISTER);
    }
}
