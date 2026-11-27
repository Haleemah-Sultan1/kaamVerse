package kaamverse.ui;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.text.Font;

/**
 * The one stylesheet of the app (forest green and gold) and its bundled fonts
 * (Lora for headings, Instrument Sans for the interface).
 * Dialogs are separate windows, so they need the stylesheet too.
 */
public final class Theme {

    private static final String[] FONTS = {
        "Lora-Regular", "Lora-Bold", "Lora-Italic", "Lora-BoldItalic",
        "InstrumentSans-Regular", "InstrumentSans-Bold", "InstrumentSans-Italic", "InstrumentSans-BoldItalic"
    };

    static {
        for (String name : FONTS) {
            loadFont(name);
        }
    }

    private static final String STYLESHEET =
            Theme.class.getResource("/kaamverse/css/theme.css").toExternalForm();

    private Theme() {
    }

    /** Call once at start-up so the fonts are registered before any screen is built. */
    public static void init() {
        // Touching the class runs the static block above.
    }

    public static void apply(Scene scene) {
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }

    public static void apply(DialogPane pane) {
        if (!pane.getStylesheets().contains(STYLESHEET)) {
            pane.getStylesheets().add(STYLESHEET);
        }
    }

    private static void loadFont(String name) {
        try (InputStream in = Theme.class.getResourceAsStream("/fonts/" + name + ".ttf")) {
            if (in != null) {
                Font.loadFont(in, 14);
            }
        } catch (IOException e) {
            System.err.println("Could not load font " + name + ": " + e.getMessage());
        }
    }
}
