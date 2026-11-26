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
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());

        getChildren().addAll(heading, new Label(Strings.COMING_SOON), back);
    }
}
