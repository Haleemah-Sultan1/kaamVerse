package kaamverse.ui;

import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.MainMenuController;
import kaamverse.bl.model.Notification;
import kaamverse.bl.service.NotificationObserver;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;

/**
 * Unread notifications with a mark-as-read button.
 * GoF Observer: refreshes when a notification is sent to this user.
 */
public class NotificationPanel extends VBox implements NotificationObserver {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM, HH:mm");

    private final MainMenuController controller;
    private final ListView<Notification> list = new ListView<>();

    public NotificationPanel(MainMenuController controller) {
        super(8);
        this.controller = controller;
        setPrefWidth(360);
        getStyleClass().add("card");
        Label heading = new Label(Strings.NOTIFICATIONS);
        heading.getStyleClass().add("section-title");

        list.setPlaceholder(new Label(Strings.NO_NOTIFICATIONS));
        list.setCellFactory(view -> new NotificationCell());

        Button markRead = new Button(Strings.MARK_AS_READ);
        markRead.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        markRead.setOnAction(event -> markSelectedAsRead());

        Button refresh = new Button(Strings.REFRESH);
        refresh.setOnAction(event -> refresh());

        VBox.setVgrow(list, javafx.scene.layout.Priority.ALWAYS);
        getChildren().addAll(heading, list, markRead, refresh);

        // Listen only while this panel is on screen.
        sceneProperty().addListener((property, old, scene) -> {
            if (scene == null) {
                controller.removeObserver(this);
            } else if (old == null) {
                controller.addObserver(this);
            }
        });
        refresh();
    }

    @Override
    public void onNotification(Notification notification) {
        if (controller.isForCurrentUser(notification)) {
            // Runs later, after any transaction around send() has finished.
            Platform.runLater(this::refresh);
        }
    }

    private void refresh() {
        try {
            list.getItems().setAll(controller.unreadNotifications());
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        }
    }

    private void markSelectedAsRead() {
        Notification selected = list.getSelectionModel().getSelectedItem();
        try {
            controller.markRead(selected);
            list.getSelectionModel().clearSelection();
            refresh();
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        }
    }

    /** Shows the time and the message, wrapped to the list's width. */
    private static final class NotificationCell extends ListCell<Notification> {

        private NotificationCell() {
            setWrapText(true);
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(Notification item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null
                    ? null
                    : TIME.format(item.getCreatedAt()) + "  " + item.getText());
        }
    }
}
