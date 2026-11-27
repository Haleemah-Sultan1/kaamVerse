package kaamverse.ui;

import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import kaamverse.bl.controller.LoginController;
import kaamverse.common.Strings;

/**
 * Swaps the screen shown in the main window.
 * GRASP Low Coupling: screens do not manage the Stage themselves.
 */
public class SceneNavigator {

    private static final double WIDTH = 1240;
    private static final double HEIGHT = 780;

    private final Stage stage;

    public SceneNavigator(Stage stage) {
        this.stage = stage;
    }

    public void show(Parent screen, String title) {
        stage.setTitle(title);
        // The green artwork background is drawn behind every screen.
        if (!screen.getStyleClass().contains("app-root")) {
            screen.getStyleClass().add("app-root");
        }
        screen.setOpacity(0);
        if (stage.getScene() == null) {
            Scene scene = new Scene(screen, WIDTH, HEIGHT);
            Theme.apply(scene);
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(680);
        } else {
            stage.getScene().setRoot(screen);
        }
        FadeTransition fadeIn = new FadeTransition(Duration.millis(320), screen);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /** The typewriter welcome screen shown when the app starts. */
    public void showIntro() {
        show(new IntroScreen(this), Strings.APP_TITLE);
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
