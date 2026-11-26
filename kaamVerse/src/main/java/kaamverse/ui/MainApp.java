package kaamverse.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.Strings;

/** JavaFX application: sets up the window and the global error handler. */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> handle(error));

        SceneNavigator navigator = new SceneNavigator(stage);
        navigator.showLogin();
        stage.show();
    }

    /** JavaFX may wrap handler errors, so look through the causes. */
    private void handle(Throwable error) {
        error.printStackTrace();
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof DuplicateRecordException) {
                AlertHelper.showError(Strings.DUPLICATE_ERROR);
                return;
            }
            if (t instanceof DatabaseException) {
                AlertHelper.showDatabaseError();
                return;
            }
        }
        AlertHelper.showError(Strings.UNEXPECTED_ERROR);
    }
}
