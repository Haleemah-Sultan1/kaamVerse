package kaamverse.ui;

import javafx.application.Application;

/**
 * Program entry point. It does not extend Application, which avoids the
 * "JavaFX runtime components are missing" error when run from an IDE.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
