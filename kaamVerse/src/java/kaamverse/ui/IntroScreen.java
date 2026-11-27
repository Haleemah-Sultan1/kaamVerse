package kaamverse.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import kaamverse.common.Strings;

/**
 * The first screen: "Kaam Verse" is typed out letter by letter, then the
 * tagline and the Enter button fade in. Enter (button or keyboard) opens the login.
 * Pressing Enter or clicking while it is still typing skips straight to the end.
 */
public class IntroScreen extends StackPane {

    private static final String KAAM = "Kaam";
    private static final String VERSE = " Verse";
    private static final String FULL = KAAM + VERSE;
    private static final double SIZE = 92;

    private final SceneNavigator navigator;
    private final Text typedKaam = Wordmark.part("", "wordmark-kaam", SIZE);
    private final Text typedVerse = Wordmark.part("", "wordmark-verse", SIZE);
    private final Region cursor = new Region();
    private final Region rule = new Region();
    private final Label tagline = new Label(Strings.INTRO_TAGLINE);
    private final Button enter = new Button(Strings.INTRO_ENTER);

    private final Timeline typing = new Timeline();
    private final Timeline blink = new Timeline();
    private boolean revealed;
    private boolean leaving;

    public IntroScreen(SceneNavigator navigator) {
        this.navigator = navigator;

        // An invisible copy of the whole word keeps the layout still while typing.
        TextFlow ghost = new TextFlow(Wordmark.part(KAAM, "wordmark-kaam", SIZE),
                Wordmark.part(VERSE, "wordmark-verse", SIZE));
        ghost.setOpacity(0);
        ghost.setMouseTransparent(true);

        TextFlow typed = new TextFlow(typedKaam, typedVerse);
        typed.setMinWidth(Region.USE_PREF_SIZE);
        typed.setMaxWidth(Region.USE_PREF_SIZE);

        cursor.getStyleClass().add("intro-cursor");
        cursor.setMinSize(4, SIZE * 0.78);
        cursor.setPrefSize(4, SIZE * 0.78);
        cursor.setMaxSize(4, SIZE * 0.78);

        HBox line = new HBox(6, typed, cursor);
        line.setAlignment(Pos.CENTER_LEFT);
        line.setMaxWidth(Region.USE_PREF_SIZE);

        StackPane word = new StackPane(ghost, line);
        word.setAlignment(Pos.CENTER_LEFT);
        word.setMaxWidth(Region.USE_PREF_SIZE);

        rule.getStyleClass().add("intro-rule");
        rule.setMinSize(260, 2);
        rule.setPrefSize(260, 2);
        rule.setMaxSize(260, 2);
        rule.setScaleX(0);

        tagline.getStyleClass().add("tagline");
        tagline.setOpacity(0);

        enter.getStyleClass().add("enter-button");
        enter.setDefaultButton(true);
        enter.setOpacity(0);
        enter.setMouseTransparent(true);
        enter.setOnAction(event -> {
            if (revealed) {
                leave();
            } else {
                skip();
            }
        });

        VBox column = new VBox(22, word, rule, tagline, enter);
        column.setAlignment(Pos.CENTER);
        column.setPadding(new Insets(0, 0, 40, 0));
        VBox.setMargin(enter, new Insets(26, 0, 0, 0));
        getChildren().add(column);
        setAlignment(Pos.CENTER);

        setOnMouseClicked(event -> {
            if (!revealed) {
                skip();
            }
        });

        startBlinking();
        startTyping();

        // Stop the animations once this screen is replaced.
        sceneProperty().addListener((property, old, scene) -> {
            if (scene == null) {
                typing.stop();
                blink.stop();
            }
        });
    }

    private void startTyping() {
        double time = 750;
        for (int count = 1; count <= FULL.length(); count++) {
            final int shown = count;
            time += 105 + (count * 37 % 50);          // a slightly uneven, human rhythm
            if (count == KAAM.length() + 1) {
                time += 220;                           // a short pause between the two words
            }
            typing.getKeyFrames().add(new KeyFrame(Duration.millis(time), event -> show(shown)));
        }
        typing.getKeyFrames().add(new KeyFrame(Duration.millis(time + 350), event -> reveal()));
        typing.play();
    }

    private void show(int count) {
        typedKaam.setText(FULL.substring(0, Math.min(count, KAAM.length())));
        typedVerse.setText(count > KAAM.length() ? FULL.substring(KAAM.length(), count) : "");
    }

    private void startBlinking() {
        blink.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, event -> cursor.setOpacity(1)),
                new KeyFrame(Duration.millis(520), event -> cursor.setOpacity(0)),
                new KeyFrame(Duration.millis(1040)));
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();
    }

    private void skip() {
        typing.stop();
        show(FULL.length());
        reveal();
    }

    private void reveal() {
        if (revealed) {
            return;
        }
        revealed = true;
        enter.setMouseTransparent(false);

        ScaleTransition grow = new ScaleTransition(Duration.millis(750), rule);
        grow.setFromX(0);
        grow.setToX(1);

        FadeTransition tag = new FadeTransition(Duration.millis(850), tagline);
        tag.setToValue(1);
        tag.setDelay(Duration.millis(350));

        FadeTransition button = new FadeTransition(Duration.millis(700), enter);
        button.setToValue(1);
        button.setDelay(Duration.millis(800));
        enter.setTranslateY(14);
        TranslateTransition lift = new TranslateTransition(Duration.millis(700), enter);
        lift.setToY(0);
        lift.setDelay(Duration.millis(800));

        new ParallelTransition(grow, tag, button, lift).play();
    }

    private void leave() {
        if (leaving) {
            return;
        }
        leaving = true;
        FadeTransition out = new FadeTransition(Duration.millis(380), this);
        out.setToValue(0);
        out.setOnFinished(event -> navigator.showLogin());
        out.play();
    }
}
