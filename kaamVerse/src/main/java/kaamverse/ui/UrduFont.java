package kaamverse.ui;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.text.Font;

/** Finds the Noto Nastaliq Urdu font for Urdu screens. */
public final class UrduFont {

    private static final String RESOURCE = "/fonts/NotoNastaliqUrdu-Regular.ttf";
    private static final String FAMILY = "Noto Nastaliq Urdu";

    private static final String STYLE = findStyle();

    private UrduFont() {
    }

    /** CSS for a screen's root; uses the default font if Nastaliq is missing. */
    public static String style() {
        return STYLE;
    }

    private static String findStyle() {
        try (InputStream in = UrduFont.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                Font font = Font.loadFont(in, 15);
                if (font != null) {
                    return css(font.getFamily());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read the Urdu font: " + e.getMessage());
        }
        return Font.getFamilies().contains(FAMILY) ? css(FAMILY) : "-fx-font-size: 15px;";
    }

    private static String css(String family) {
        return "-fx-font-family: '" + family + "'; -fx-font-size: 15px;";
    }
}
