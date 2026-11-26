package kaamverse.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.LoginController;
import kaamverse.bl.controller.MainMenuController;
import kaamverse.common.Strings;

/** The menu for the logged-in user, built from both menu modules. */
public class MainMenuScreen extends BorderPane {

    public MainMenuScreen(SceneNavigator navigator) {
        MainMenuController controller = new MainMenuController();
        setPadding(new Insets(24));

        Label who = new Label(Strings.loggedInAs(controller.userName(), controller.role()));
        who.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button logout = new Button(Strings.LOGOUT);
        logout.setOnAction(event -> {
            new LoginController().logout();
            navigator.showLogin();
        });
        HBox top = new HBox(12, who, space, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 16, 0));

        List<MenuEntry> entries = new ArrayList<>();
        entries.addAll(MenuModuleA.itemsFor(controller.role(), controller.permissions()));
        entries.addAll(MenuModuleB.itemsFor(controller.role(), controller.permissions()));

        VBox menu = new VBox(10);
        menu.setPadding(new Insets(0, 24, 0, 0));
        for (MenuEntry entry : entries) {
            Button button = new Button(entry.title());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event ->
                    navigator.show(entry.screen().apply(navigator), Strings.APP_TITLE));
            menu.getChildren().add(button);
        }

        setTop(top);
        setCenter(menu);
        setRight(new NotificationPanel(controller));
    }
}
