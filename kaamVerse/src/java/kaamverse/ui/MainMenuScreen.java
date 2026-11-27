package kaamverse.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.LoginController;
import kaamverse.bl.controller.MainMenuController;
import kaamverse.common.Strings;

/** The menu for the logged-in user, built from both menu modules, as numbered glass tiles. */
public class MainMenuScreen extends BorderPane {

    public MainMenuScreen(SceneNavigator navigator) {
        MainMenuController controller = new MainMenuController();
        setPadding(new Insets(28, 40, 32, 40));

        Label who = new Label(Strings.loggedInAs(controller.userName(), controller.role()));
        who.getStyleClass().add("chip");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button logout = new Button(Strings.LOGOUT);
        logout.setOnAction(event -> {
            new LoginController().logout();
            navigator.showLogin();
        });
        HBox top = new HBox(14, Wordmark.create(34), space, who, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 10, 0));

        List<MenuEntry> entries = new ArrayList<>();
        entries.addAll(MenuModuleA.itemsFor(controller.role(), controller.permissions()));
        entries.addAll(MenuModuleB.itemsFor(controller.role(), controller.permissions()));

        TilePane grid = new TilePane(18, 18);
        grid.setPrefTileWidth(250);
        grid.setPadding(new Insets(14, 8, 24, 4));
        int number = 1;
        for (MenuEntry entry : entries) {
            grid.getChildren().add(card(navigator, entry, number++));
        }
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label heading = new Label(Strings.MENU_HEADING);
        heading.getStyleClass().addAll("screen-title", "serif");
        Region bar = new Region();
        bar.getStyleClass().add("accent-bar");
        bar.setMinSize(56, 3);
        bar.setPrefSize(56, 3);
        bar.setMaxSize(56, 3);

        VBox center = new VBox(10, heading, bar, scroll);
        center.setPadding(new Insets(14, 0, 0, 0));
        BorderPane.setMargin(center, new Insets(0, 28, 0, 0));

        setTop(top);
        setCenter(center);
        setRight(new NotificationPanel(controller));
    }

    private static Button card(SceneNavigator navigator, MenuEntry entry, int number) {
        Label index = new Label(String.format("%02d", number));
        index.getStyleClass().add("menu-index");
        Label title = new Label(entry.title());
        title.getStyleClass().add("menu-title");
        title.setWrapText(true);
        title.setMaxWidth(205);
        Region fill = new Region();
        VBox.setVgrow(fill, Priority.ALWAYS);
        Label arrow = new Label("\u2192");
        arrow.getStyleClass().add("menu-arrow");
        VBox content = new VBox(10, index, title, fill, arrow);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPrefSize(210, 108);
        content.setMinSize(210, 108);
        content.setMaxSize(210, 108);

        Button card = new Button();
        card.getStyleClass().add("menu-card");
        card.setGraphic(content);
        card.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        card.setAccessibleText(entry.title());
        card.setOnAction(event -> navigator.show(entry.screen().apply(navigator), Strings.APP_TITLE));
        return card;
    }
}
