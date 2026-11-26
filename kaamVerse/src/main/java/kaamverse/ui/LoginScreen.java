package kaamverse.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import kaamverse.bl.controller.LoginController;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.UserRole;

/** Role selector and credentials. */
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

        Label heading = new Label(Strings.WELCOME);
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

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

        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label(Strings.ROLE_LABEL), role);
        form.addRow(1, identifierLabel, identifier);
        form.addRow(2, secretLabel, secret);

        Button login = new Button(Strings.LOGIN);
        login.setDefaultButton(true);
        login.setOnAction(event -> logIn());

        Button register = new Button(Strings.UC1_REGISTER);
        register.setOnAction(event ->
                navigator.show(MenuModuleA.registrationScreen(navigator), Strings.APP_TITLE));

        getChildren().addAll(heading, form, login, register);
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
