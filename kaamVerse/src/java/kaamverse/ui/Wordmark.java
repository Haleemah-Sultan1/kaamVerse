package kaamverse.ui;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/** The "Kaam" + "Verse" logotype: cream serif, then gold italic serif. */
public final class Wordmark {

    private Wordmark() {
    }

    /** The full logotype at the given font size (px). */
    public static TextFlow create(double size) {
        return new TextFlow(part("Kaam", "wordmark-kaam", size), part("Verse", "wordmark-verse", size));
    }

    /** One styled piece of the logotype; also used by the typewriter intro. */
    public static Text part(String text, String styleClass, double size) {
        Text piece = new Text(text);
        piece.getStyleClass().add(styleClass);
        piece.setStyle("-fx-font-size: " + size + "px;");
        return piece;
    }
}
