package kaamverse.ui;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import javafx.scene.Parent;
import kaamverse.common.Permission;
import kaamverse.common.UserRole;

/**
 * One button on the main menu: its text, who may see it, and the screen it opens.
 */
public record MenuEntry(String title, Set<UserRole> roles, Set<Permission> permissions,
                        Function<SceneNavigator, Parent> screen) {

    /** An entry for maids or hiring women. */
    public static MenuEntry forRole(UserRole role, String title, Function<SceneNavigator, Parent> screen) {
        return new MenuEntry(title, Set.of(role), Set.of(), screen);
    }

    /** An entry for officers who hold the given permission. */
    public static MenuEntry forPermission(Permission permission, String title,
                                          Function<SceneNavigator, Parent> screen) {
        return new MenuEntry(title, Set.of(), Set.of(permission), screen);
    }

    public boolean isAllowed(UserRole role, Set<Permission> granted) {
        return roles.contains(role) || !Collections.disjoint(permissions, granted);
    }
}
