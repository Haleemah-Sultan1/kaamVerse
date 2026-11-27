package kaamverse.ui;

import java.util.List;
import java.util.Set;

import kaamverse.common.Permission;
import kaamverse.common.Strings;
import kaamverse.common.UserRole;
import kaamverse.common.StringsB;
/**
 * Person B's menu entries. Replace ComingSoonScreen with your own screen,
 * for example: navigator -> new MyScreen(navigator)
 */
public final class MenuModuleB {

    private static final List<MenuEntry> ENTRIES = List.of(
            // UC3
            // UC3
            MenuEntry.forRole(UserRole.HIRING_WOMAN, Strings.UC3_HIRE,
                    navigator -> new HireSearchScreen(navigator)),
            MenuEntry.forRole(UserRole.HIRING_WOMAN, StringsB.UC3_MY_HIRES,
                    navigator -> new MyHiresScreen(navigator)),
            // UC4
            MenuEntry.forRole(UserRole.HIRING_WOMAN, Strings.UC4_EXPERIENCE,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC4_EXPERIENCE)),
            // UC5
            MenuEntry.forPermission(Permission.AWARD_BADGES, Strings.UC5_BADGE,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC5_BADGE)),
            // UC6
            MenuEntry.forPermission(Permission.MANAGE_WAGE_BANDS, Strings.UC6_WAGES,
                    navigator -> new WageBandScreen(navigator)),
            // UC11
            MenuEntry.forRole(UserRole.MAID, Strings.UC11_COMPLAINT,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC11_COMPLAINT)),
            // UC12
            MenuEntry.forPermission(Permission.REVIEW_COMPLAINTS, Strings.UC12_REVIEW_COMPLAINTS,
                    navigator -> new ComingSoonScreen(navigator, Strings.UC12_REVIEW_COMPLAINTS)));

    private MenuModuleB() {
    }

    /** The entries this user is allowed to see. */
    public static List<MenuEntry> itemsFor(UserRole role, Set<Permission> permissions) {
        return ENTRIES.stream()
                .filter(entry -> entry.isAllowed(role, permissions))
                .toList();
    }
}
