package kaamverse.bl.model;

import kaamverse.common.UserRole;

/** Anyone who can log in. GRASP Low Coupling: Session and notifications need only this. */
public interface User {

    int getId();

    String getName();

    UserRole getUserRole();
}
