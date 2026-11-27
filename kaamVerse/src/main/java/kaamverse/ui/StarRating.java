package kaamverse.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

// Five clickable stars; getRating() is 0 until one is clicked.
public class StarRating extends HBox {

    private static final String FILLED = "\u2605";
    private static final String EMPTY = "\u2606";

    private final List<Label> stars = new ArrayList<>();
    private int rating;

    public StarRating() {
        super(4);
        for (int i = 1; i <= 5; i++) {
            int value = i;
            Label star = new Label(EMPTY);
            star.setStyle("-fx-font-size: 22px; -fx-cursor: hand;");
            star.setOnMouseClicked(event -> setRating(value));
            stars.add(star);
            getChildren().add(star);
        }
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int value) {
        rating = value;
        for (int i = 0; i < stars.size(); i++) {
            stars.get(i).setText(i < rating ? FILLED : EMPTY);
        }
    }
}