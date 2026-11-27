package kaamverse.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import kaamverse.bl.controller.LoginController;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.UserRole;

/** Role selector and credentials, on a frosted card over the green artwork. */
public class LoginScreen extends VBox {

    private final SceneNavigator navigator;
    private final LoginController controller = new LoginController();

    private final ComboBox<UserRole> role = new ComboBox<>();
    private final Label identifierLabel = new Label();
    private final Label secretLabel = new Label();
    private final TextField identifier = new TextField();
    private final PasswordField secret = new PasswordField();

    public LoginScreen(SceneNavigator navigator) {
        super(16);
        this.navigator = navigator;
        setAlignment(Pos.CENTER);
        setPadding(new Insets(24));

        Label heading = new Label(Strings.WELCOME_BACK);
        heading.getStyleClass().addAll("screen-title", "serif");
        Label hint = new Label(Strings.SIGN_IN_HINT);
        hint.getStyleClass().add("muted");
        VBox head = new VBox(4, heading, hint);

        role.getItems().setAll(UserRole.values());
        role.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserRole value) {
                return value == null ? "" : Strings.roleName(value);
            }

            @Override
            public UserRole fromString(String text) {
                return null; // the list is not editable
            }
        });
        role.valueProperty().addListener((property, old, chosen) -> showLabelsFor(chosen));
        role.setValue(UserRole.MAID);
        role.setMaxWidth(Double.MAX_VALUE);

        Label roleLabel = new Label(Strings.ROLE_LABEL);
        identifierLabel.getStyleClass().add("field-label");
        secretLabel.getStyleClass().add("field-label");
        roleLabel.getStyleClass().add("field-label");
        identifier.setMaxWidth(Double.MAX_VALUE);
        secret.setMaxWidth(Double.MAX_VALUE);

        Button login = new Button(Strings.LOGIN);
        login.setDefaultButton(true);
        login.setOnAction(event -> logIn());

        Button register = new Button(Strings.UC1_REGISTER);
        register.setOnAction(event ->
                navigator.show(MenuModuleA.registrationScreen(navigator), Strings.APP_TITLE));

        login.setMaxWidth(Double.MAX_VALUE);
        register.setMaxWidth(Double.MAX_VALUE);
        login.setStyle("-fx-padding: 12 20; -fx-font-size: 15px;");
        register.setStyle("-fx-padding: 11 20;");

        VBox formSide = new VBox(16, head,
                field(roleLabel, role),
                field(identifierLabel, identifier),
                field(secretLabel, secret),
                login, divider(), register);
        formSide.setAlignment(Pos.CENTER_LEFT);
        formSide.setPadding(new Insets(44, 48, 44, 44));
        formSide.setPrefWidth(460);
        VBox.setMargin(login, new Insets(6, 0, 0, 0));
        HBox.setHgrow(formSide, Priority.ALWAYS);

        HBox card = new HBox(hero(), formSide);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(1000);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        getChildren().add(card);
    }

    /** A label above its control. */
    private static VBox field(Label label, Node control) {
        return new VBox(6, label, control);
    }

    /** A thin rule with a small "or" in the middle. */
    private static HBox divider() {
        Region left = new Region();
        Region right = new Region();
        left.getStyleClass().add("rule");
        right.getStyleClass().add("rule");
        left.setPrefHeight(1);
        right.setPrefHeight(1);
        left.setMaxHeight(1);
        right.setMaxHeight(1);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        Label or = new Label(Strings.OR);
        or.getStyleClass().add("muted");
        HBox row = new HBox(12, left, or, right);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    /** Brand panel: the logotype, a gold bar, the tagline and two soft rings. */
    private static StackPane hero() {
        VBox text = new VBox(18, Wordmark.create(60), goldBar(), tagline());
        text.setAlignment(Pos.CENTER_LEFT);
        text.setPadding(new Insets(0, 36, 0, 52));

        Circle ring = new Circle(150, Color.TRANSPARENT);
        ring.setStroke(Color.web("#D8B45F", 0.30));
        ring.setStrokeWidth(1.2);
        Circle ringInner = new Circle(105, Color.TRANSPARENT);
        ringInner.setStroke(Color.web("#D8B45F", 0.16));
        ringInner.setStrokeWidth(1);
        Circle dot = new Circle(60, Color.web("#D8B45F", 0.10));
        StackPane.setAlignment(ring, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(ringInner, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(dot, Pos.TOP_RIGHT);
        StackPane.setMargin(ring, new Insets(0, -60, -60, 0));
        StackPane.setMargin(ringInner, new Insets(0, -15, -15, 0));
        StackPane.setMargin(dot, new Insets(36, 36, 0, 0));
        for (Node decoration : new Node[] {ring, ringInner, dot}) {
            decoration.setMouseTransparent(true);
        }

        StackPane hero = new StackPane(dot, ringInner, ring, text);
        hero.getStyleClass().add("hero");
        hero.setPrefWidth(470);
        hero.setMinWidth(380);
        hero.setMinHeight(500);
        hero.setAlignment(Pos.CENTER_LEFT);
        // Keep the rings inside the rounded panel.
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(52);
        clip.setArcHeight(52);
        clip.widthProperty().bind(hero.widthProperty().add(40));
        clip.heightProperty().bind(hero.heightProperty());
        hero.setClip(clip);
        StackPane.setAlignment(text, Pos.CENTER_LEFT);
        return hero;
    }

    private static Region goldBar() {
        Region bar = new Region();
        bar.getStyleClass().add("accent-bar");
        bar.setMinSize(72, 4);
        bar.setPrefSize(72, 4);
        bar.setMaxSize(72, 4);
        return bar;
    }

    private static Label tagline() {
        Label tagline = new Label(Strings.INTRO_TAGLINE);
        tagline.getStyleClass().add("tagline");
        tagline.setWrapText(true);
        return tagline;
    }

    private void showLabelsFor(UserRole chosen) {
        boolean officer = chosen == UserRole.OFFICER;
        identifierLabel.setText(officer ? Strings.USERNAME_LABEL : Strings.PHONE_LABEL);
        secretLabel.setText(officer ? Strings.PASSWORD_LABEL : Strings.PIN_LABEL);
        identifier.clear();
        secret.clear();
    }

    private void logIn() {
        if (identifier.getText().isBlank() || secret.getText().isEmpty()) {
            AlertHelper.showError(Strings.MISSING_CREDENTIALS);
            return;
        }
        try {
            if (controller.login(role.getValue(), identifier.getText(), secret.getText())) {
                navigator.showMainMenu();
            } else {
                secret.clear();
                AlertHelper.showError(role.getValue() == UserRole.OFFICER
                        ? Strings.WRONG_OFFICER_LOGIN : Strings.WRONG_PIN_LOGIN);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        }
    }
}
