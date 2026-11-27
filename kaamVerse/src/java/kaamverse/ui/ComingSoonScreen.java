package kaamverse.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import kaamverse.common.Strings;

/** Placeholder for a use case that is not built yet. */
public class ComingSoonScreen extends VBox {

    public ComingSoonScreen(SceneNavigator navigator, String title) {
        super(16);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(24));

        Label heading = new Label(title);
        heading.getStyleClass().addAll("screen-title", "serif");
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());

        Label soon = new Label(Strings.COMING_SOON);
        soon.getStyleClass().add("muted");
        getChildren().addAll(heading, soon, back);
    }
}
