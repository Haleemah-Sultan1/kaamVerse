package kaamverse.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import kaamverse.common.Strings;

/** The single place where pop-up alerts are created. */
public final class AlertHelper {

    private AlertHelper() {
    }

    public static void showError(String message) {
        create(Alert.AlertType.ERROR, Strings.ERROR_TITLE, message).showAndWait();
    }

    public static void showDatabaseError() {
        showError(Strings.DATABASE_ERROR);
    }

    public static void showInfo(String message) {
        create(Alert.AlertType.INFORMATION, Strings.INFO_TITLE, message).showAndWait();
    }

    public static boolean confirm(String message) {
        return create(Alert.AlertType.CONFIRMATION, Strings.CONFIRM_TITLE, message)
                .showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isPresent();
    }

    private static Alert create(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert;
    }
}
