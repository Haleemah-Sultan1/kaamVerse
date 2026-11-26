package kaamverse.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kaamverse.bl.controller.LoginController;
import kaamverse.common.Strings;

/**
 * Swaps the screen shown in the main window.
 * GRASP Low Coupling: screens do not manage the Stage themselves.
 */
public class SceneNavigator {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;

    private final Stage stage;

    public SceneNavigator(Stage stage) {
        this.stage = stage;
    }

    public void show(Parent screen, String title) {
        stage.setTitle(title);
        if (stage.getScene() == null) {
            stage.setScene(new Scene(screen, WIDTH, HEIGHT));
        } else {
            stage.getScene().setRoot(screen);
        }
    }

    public void showLogin() {
        show(new LoginScreen(this), Strings.APP_TITLE);
    }

    public void showMainMenu() {
        show(new MainMenuScreen(this), Strings.APP_TITLE);
    }

    /** For Back buttons: the main menu if logged in, otherwise the login screen. */
    public void goHome() {
        if (new LoginController().isLoggedIn()) {
            showMainMenu();
        } else {
            showLogin();
        }
    }
}
