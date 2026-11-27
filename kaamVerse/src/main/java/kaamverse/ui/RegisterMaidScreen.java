package kaamverse.ui;

import java.io.File;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import kaamverse.bl.controller.RegistrationController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.Locality;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.RegistrationField;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.Street;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.Strings;

/** UC1: step-by-step Urdu registration, right to left. */
public class RegisterMaidScreen extends BorderPane {

    private enum Step { DETAILS, LOCALITY, STREET, SERVICES, IMAGE, REVIEW }

    private final SceneNavigator navigator;
    private final RegistrationController controller = new RegistrationController();

    // Inputs are kept as fields so that going back keeps what was entered.
    private final Map<RegistrationField, TextField> details = new EnumMap<>(RegistrationField.class);
    private final ComboBox<Locality> localities = new ComboBox<>();
    private final ComboBox<Street> streets = new ComboBox<>();
    private final Map<Integer, CheckBox> serviceBoxes = new LinkedHashMap<>();
    private final Map<Integer, TextField> serviceYears = new LinkedHashMap<>();
    private final VBox serviceList = new VBox(8);
    private final ImageView preview = new ImageView();
    private final Label imageName = new Label(Strings.UC1_NO_IMAGE);

    private final Label heading = new Label();
    private final Label prompt = new Label();
    private final ScrollPane content = new ScrollPane();
    private final Button back = new Button(Strings.UC1_BACK);
    private final Button next = new Button();
    private Step step = Step.DETAILS;

    public RegisterMaidScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        setStyle(UrduFont.style());
        setPadding(new Insets(16, 24, 16, 24));
        controller.startNewRegistration();
        buildInputs();

        heading.setStyle("-fx-font-size: 20px;");
        prompt.setWrapText(true);
        Button listen = new Button(Strings.UC1_LISTEN);
        listen.setOnAction(event -> controller.playPrompt(prompt.getText()));
        HBox promptRow = new HBox(12, prompt, listen);
        promptRow.setAlignment(Pos.CENTER_LEFT);

        content.setFitToWidth(true);
        content.setStyle("-fx-background-color: transparent;");

        Button cancel = new Button(Strings.UC1_CANCEL);
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> navigator.goHome());
        back.setOnAction(event -> show(Step.values()[step.ordinal() - 1]));
        next.setDefaultButton(true);
        next.setOnAction(event -> goForward());
        HBox buttons = new HBox(10, next, back, cancel);

        setTop(new VBox(4, heading, promptRow));
        setCenter(content);
        setBottom(buttons);
        show(Step.DETAILS);
    }

    private void buildInputs() {
        for (RegistrationField field : RegistrationField.values()) {
            TextField input = field == RegistrationField.PIN_HASH ? new PasswordField() : new TextField();
            // Numbers read left to right even on a right-to-left screen.
            if (field == RegistrationField.CNIC || field == RegistrationField.PHONE
                    || field == RegistrationField.PIN_HASH) {
                input.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
            input.setPrefWidth(360);
            details.put(field, input);
        }
        details.get(RegistrationField.CNIC).setPromptText("00000-0000000-0");
        details.get(RegistrationField.PHONE).setPromptText("03000000000");

        localities.setConverter(converter(Locality::getName));
        streets.setConverter(converter(Street::getName));
        localities.setPrefWidth(300);
        streets.setPrefWidth(300);

        preview.setFitWidth(320);
        preview.setFitHeight(200);
        preview.setPreserveRatio(true);
    }

    private static <T> StringConverter<T> converter(Function<T, String> name) {
        return new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : name.apply(item);
            }

            @Override
            public T fromString(String text) {
                return null; // the list is not editable
            }
        };
    }

    private void show(Step target) {
        step = target;
        switch (target) {
            case DETAILS -> present(Strings.UC1_STEP_DETAILS, Strings.UC1_PROMPT_DETAILS, detailsForm());
            case LOCALITY -> present(Strings.UC1_STEP_LOCALITY, Strings.UC1_PROMPT_LOCALITY,
                    row(Strings.UC1_LOCALITY, localities));
            case STREET -> present(Strings.UC1_STEP_STREET, Strings.UC1_PROMPT_STREET,
                    row(Strings.UC1_STREET, streets));
            case SERVICES -> present(Strings.UC1_STEP_SERVICES, Strings.UC1_PROMPT_SERVICES, serviceList);
            case IMAGE -> present(Strings.UC1_STEP_IMAGE, Strings.UC1_PROMPT_IMAGE, imagePicker());
            case REVIEW -> present(Strings.UC1_STEP_REVIEW, Strings.UC1_PROMPT_REVIEW, review());
        }
        back.setDisable(target == Step.DETAILS);
        next.setText(target == Step.REVIEW ? Strings.UC1_SUBMIT : Strings.UC1_NEXT);
    }

    private void present(String title, String instruction, javafx.scene.Node body) {
        heading.setText(title);
        prompt.setText(instruction);
        VBox holder = new VBox(body);
        holder.setPadding(new Insets(8, 0, 8, 0));
        content.setContent(holder);
        content.setVvalue(0);
        controller.playPrompt(instruction);
    }

    /** Validates the current step and, if it is correct, moves on. */
    private void goForward() {
        attempt(() -> {
            switch (step) {
                case DETAILS -> {
                    for (RegistrationField field : RegistrationField.values()) {
                        controller.enterFieldValue(field, details.get(field).getText());
                    }
                    if (localities.getItems().isEmpty()) {
                        localities.getItems().setAll(controller.getLocalities());
                    }
                    show(Step.LOCALITY);
                }
                case LOCALITY -> {
                    List<Street> found = controller.selectLocality(localities.getValue());
                    Street chosen = streets.getValue();
                    streets.getItems().setAll(found);
                    streets.setValue(found.stream()
                            .filter(s -> chosen != null && s.getStreetId() == chosen.getStreetId())
                            .findFirst().orElse(null));
                    show(Step.STREET);
                }
                case STREET -> {
                    controller.selectStreet(streets.getValue());
                    if (serviceBoxes.isEmpty()) {
                        buildServiceList(controller.getServices());
                    }
                    show(Step.SERVICES);
                }
                case SERVICES -> {
                    Map<Integer, String> chosen = new LinkedHashMap<>();
                    serviceBoxes.forEach((id, box) -> {
                        if (box.isSelected()) {
                            chosen.put(id, serviceYears.get(id).getText());
                        }
                    });
                    controller.selectServicesExperience(chosen);
                    show(Step.IMAGE);
                }
                case IMAGE -> {
                    controller.uploadCnicImage(controller.getCnicImage());
                    show(Step.REVIEW);
                }
                case REVIEW -> submit();
            }
        });
    }

    private void submit() {
        controller.submitRegistration();
        controller.playPrompt(Strings.UC1_SUCCESS);
        AlertHelper.showInfo(Strings.UC1_SUCCESS);
        navigator.goHome();
    }

    /** Runs an action and turns each kind of failure into a friendly alert. */
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            if (e.getField() != null) {
                show(Step.DETAILS);
                details.get(e.getField()).requestFocus();
            }
        } catch (DuplicateRecordException e) {
            AlertHelper.showError(Strings.UC1_ERR_DUPLICATE);
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UC1_ERR_DATABASE);
        } catch (java.io.UncheckedIOException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UC1_ERR_IMAGE_SAVE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UNEXPECTED_ERROR);
        }
    }

    private GridPane detailsForm() {
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(2);
        String[] labels = {Strings.UC1_NAME, Strings.UC1_FATHER_NAME, Strings.UC1_CNIC,
                Strings.UC1_PHONE, Strings.UC1_ADDRESS, Strings.UC1_PIN};
        for (RegistrationField field : RegistrationField.values()) {
            form.addRow(field.ordinal(), new Label(labels[field.ordinal()]), details.get(field));
        }
        return form;
    }

    private static HBox row(String label, javafx.scene.Node input) {
        HBox row = new HBox(16, new Label(label), input);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void buildServiceList(List<Service> services) {
        for (Service service : services) {
            CheckBox box = new CheckBox(service.getName());
            box.setPrefWidth(200);
            TextField years = new TextField();
            years.setPrefWidth(80);
            years.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            years.disableProperty().bind(box.selectedProperty().not());
            serviceBoxes.put(service.getServiceId(), box);
            serviceYears.put(service.getServiceId(), years);
            HBox line = new HBox(16, box, new Label(Strings.UC1_YEARS), years);
            line.setAlignment(Pos.CENTER_LEFT);
            serviceList.getChildren().add(line);
        }
    }

    private VBox imagePicker() {
        Button choose = new Button(Strings.UC1_CHOOSE_IMAGE);
        choose.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(Strings.UC1_CHOOSE_IMAGE);
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(Strings.UC1_IMAGE_FILES, "*.jpg", "*.jpeg", "*.png"));
            File file = chooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                useImage(file);
            }
        });
        return new VBox(10, choose, imageName, preview);
    }

    private void useImage(File file) {
        attempt(() -> {
            Image image = new Image(file.toURI().toString());
            if (image.isError()) {
                throw new ValidationException(Strings.UC1_ERR_IMAGE);
            }
            controller.uploadCnicImage(file);
            preview.setImage(image);
            imageName.setText(file.getName());
        });
    }

    private VBox review() {
        Maid draft = controller.getDraft();
        GridPane summary = new GridPane();
        summary.setHgap(16);
        int line = 0;
        summary.addRow(line++, new Label(Strings.UC1_NAME), new Label(draft.getName()));
        summary.addRow(line++, new Label(Strings.UC1_FATHER_NAME), new Label(draft.getFatherName()));
        summary.addRow(line++, new Label(Strings.UC1_CNIC), number(draft.getCnic()));
        summary.addRow(line++, new Label(Strings.UC1_PHONE), number(draft.getPhone()));
        summary.addRow(line++, new Label(Strings.UC1_ADDRESS), new Label(draft.getAddress()));
        summary.addRow(line++, new Label(Strings.UC1_PIN), number("****"));
        summary.addRow(line++, new Label(Strings.UC1_LOCALITY), new Label(controller.getLocality().getName()));
        summary.addRow(line++, new Label(Strings.UC1_STREET), new Label(controller.getStreet().getName()));
        for (Map.Entry<String, Integer> chosen : controller.getChosenServices().entrySet()) {
            summary.addRow(line++, new Label(chosen.getKey()),
                    new Label(Strings.UC1_YEARS_VALUE.formatted(chosen.getValue())));
        }
        ImageView small = new ImageView(preview.getImage());
        small.setFitWidth(240);
        small.setFitHeight(150);
        small.setPreserveRatio(true);
        return new VBox(10, summary, small);
    }

    private static Label number(String text) {
        Label label = new Label(text);
        label.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        return label;
    }
}
