package kaamverse.ui;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import kaamverse.bl.controller.ComplaintController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.HiringWoman;
import kaamverse.common.DatabaseException;
import kaamverse.common.ProblemType;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;

public class FileComplaintScreen extends BorderPane {

    private enum Step { INTRO, WHO, DESCRIBE, DATE, CONFIRM, DONE }

    private static final int COUNTED_STEPS = 5;

    private final SceneNavigator navigator;
    private final ComplaintController controller = new ComplaintController();

    // inputs are fields so that going back keeps what was entered
    private final CheckBox agree = new CheckBox(StringsB.UC11_AGREE);
    private final ComboBox<HiringWoman> employer = new ComboBox<>();
    private final ToggleGroup typeGroup = new ToggleGroup();
    private final Label audioName = new Label(StringsB.UC11_NO_FILE);
    private final TextArea written = new TextArea();
    private final DatePicker incidentDate = new DatePicker();
    private final Label evidenceName = new Label(StringsB.UC11_NO_FILE);
    private final Map<Step, Node> inputPanes = new EnumMap<>(Step.class);
    private Path audioPath;
    private Path evidencePath;
    private String confidentiality = "";

    private final Label progress = new Label();
    private final Label heading = new Label();
    private final Label prompt = new Label();
    private final StackPane stack = new StackPane();
    private final ScrollPane content = new ScrollPane(stack);
    private final Button back = new Button(StringsB.UC11_BACK);
    private final Button next = new Button();
    private final Button cancel = new Button(StringsB.UC11_CANCEL);
    private Step step = Step.INTRO;

    public FileComplaintScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        setStyle(UrduFont.style());
        setPadding(new Insets(16, 24, 16, 24));

        // a maid who may not complain gets a message instead of the wizard
        try {
            confidentiality = controller.reportProblem();
        } catch (ValidationException e) {
            showBlocked(e.getMessage());
            return;
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showError(StringsB.UC11_ERR_DATABASE);
            showBlocked(StringsB.UC11_ERR_DATABASE);
            return;
        }

        buildInputs();

        progress.setStyle("-fx-text-fill: #666666;");
        heading.setStyle("-fx-font-size: 20px;");
        prompt.setWrapText(true);
        Button listen = new Button(StringsB.UC11_LISTEN);
        listen.setOnAction(event -> controller.playPrompt(prompt.getText()));
        HBox promptRow = new HBox(12, prompt, listen);
        promptRow.setAlignment(Pos.CENTER_LEFT);

        stack.setAlignment(Pos.TOP_CENTER);
        content.setFitToWidth(true);
        content.setStyle("-fx-background-color: transparent;");

        cancel.setCancelButton(true);
        cancel.setOnAction(event -> navigator.goHome());
        back.setOnAction(event -> show(Step.values()[step.ordinal() - 1]));
        next.setDefaultButton(true);
        next.setOnAction(event -> goForward());
        HBox buttons = new HBox(10, next, back, cancel);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        setTop(new VBox(4, progress, heading, promptRow));
        setCenter(content);
        setBottom(buttons);
        show(Step.INTRO);
    }

    private void showBlocked(String message) {
        Label title = new Label(StringsB.UC11_TITLE);
        title.setStyle("-fx-font-size: 20px;");
        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(600);
        Button listen = new Button(StringsB.UC11_LISTEN);
        listen.setOnAction(event -> controller.playPrompt(message));
        Button home = new Button(StringsB.UC11_BACK);
        home.setDefaultButton(true);
        home.setOnAction(event -> navigator.goHome());
        VBox box = new VBox(16, title, text, new HBox(10, home, listen));
        box.setAlignment(Pos.CENTER_LEFT);
        setCenter(box);
        controller.playPrompt(message);
    }

    private void buildInputs() {
        agree.setStyle("-fx-font-size: 16px;");
        inputPanes.put(Step.INTRO, new VBox(agree));

        employer.setPrefWidth(320);
        employer.setConverter(new StringConverter<HiringWoman>() {
            @Override
            public String toString(HiringWoman woman) {
                return woman == null ? "" : woman.getName();
            }

            @Override
            public HiringWoman fromString(String text) {
                return null;
            }
        });
        FlowPane types = new FlowPane(16, 16);
        for (ProblemType type : ProblemType.values()) {
            ToggleButton choice = new ToggleButton(StringsB.problemName(type));
            choice.setUserData(type);
            choice.setToggleGroup(typeGroup);
            choice.setPrefSize(190, 60);
            choice.setStyle("-fx-font-size: 16px;");
            types.getChildren().add(choice);
        }
        inputPanes.put(Step.WHO, new VBox(12, new Label(StringsB.UC11_EMPLOYER), employer,
                new Label(StringsB.UC11_PROBLEM), types));

        Button chooseAudio = new Button(StringsB.UC11_CHOOSE_AUDIO);
        chooseAudio.setOnAction(event -> pickAudio());
        ltr(audioName);
        written.setWrapText(true);
        written.setPrefRowCount(6);
        written.setPrefWidth(560);
        written.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= ComplaintController.MAX_TRANSCRIPT ? change : null));
        inputPanes.put(Step.DESCRIBE, new VBox(10, new Label(StringsB.UC11_AUDIO),
                new HBox(12, chooseAudio, audioName), new Label(StringsB.UC11_WRITTEN), written));

        // dates read left to right even on a right-to-left screen
        incidentDate.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        incidentDate.setEditable(false);
        incidentDate.setPrefWidth(260);
        incidentDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(!empty && date.isAfter(LocalDate.now()));
            }
        });
        Button chooseEvidence = new Button(StringsB.UC11_CHOOSE_EVIDENCE);
        chooseEvidence.setOnAction(event -> pickEvidence());
        ltr(evidenceName);
        GridPane dateForm = new GridPane();
        dateForm.setHgap(16);
        dateForm.setVgap(12);
        dateForm.addRow(0, new Label(StringsB.UC11_INCIDENT_DATE), incidentDate);
        dateForm.addRow(1, new Label(StringsB.UC11_EVIDENCE), new HBox(12, chooseEvidence, evidenceName));
        inputPanes.put(Step.DATE, dateForm);
    }

    private void pickAudio() {
        Path file = choose(StringsB.UC11_CHOOSE_AUDIO, StringsB.UC11_FILTER_AUDIO,
                "*.wav", "*.mp3", "*.m4a", "*.ogg");
        if (file != null) {
            audioPath = file;
            audioName.setText(file.getFileName().toString());
        }
    }

    private void pickEvidence() {
        Path file = choose(StringsB.UC11_CHOOSE_EVIDENCE, StringsB.UC11_FILTER_EVIDENCE,
                "*.jpg", "*.jpeg", "*.png", "*.pdf");
        if (file != null) {
            evidencePath = file;
            evidenceName.setText(file.getFileName().toString());
        }
    }

    private Path choose(String title, String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(StringsB.UC11_FILTER_ALL, "*.*"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        return file == null ? null : file.toPath();
    }

    private ProblemType selectedType() {
        Toggle selected = typeGroup.getSelectedToggle();
        return selected == null ? null : (ProblemType) selected.getUserData();
    }

    private void loadEmployers() {
        int keepId = employer.getValue() == null ? -1 : employer.getValue().getHiringWomanId();
        employer.getItems().setAll(controller.getEmployers());
        for (HiringWoman item : employer.getItems()) {
            if (item.getHiringWomanId() == keepId) {
                employer.setValue(item);
            }
        }
    }

    private void clearInputs() {
        agree.setSelected(false);
        employer.setValue(null);
        employer.getItems().clear();
        typeGroup.selectToggle(null);
        audioPath = null;
        audioName.setText(StringsB.UC11_NO_FILE);
        written.clear();
        incidentDate.setValue(null);
        evidencePath = null;
        evidenceName.setText(StringsB.UC11_NO_FILE);
    }

    private void show(Step target) {
        step = target;
        heading.setText(title(target));
        prompt.setText(promptText(target));
        progress.setText(target == Step.DONE ? ""
                : StringsB.UC11_STEP_OF.formatted(target.ordinal() + 1, COUNTED_STEPS));
        stack.getChildren().setAll(body(target));
        content.setVvalue(0);

        boolean done = target == Step.DONE;
        back.setDisable(target == Step.INTRO);
        back.setVisible(!done);
        back.setManaged(!done);
        cancel.setVisible(!done);
        cancel.setManaged(!done);
        next.setText(switch (target) {
            case CONFIRM -> StringsB.UC11_SUBMIT;
            case DONE -> StringsB.UC11_FINISH;
            default -> StringsB.UC11_NEXT;
        });
        controller.playPrompt(prompt.getText());
    }

    private static String title(Step target) {
        return switch (target) {
            case INTRO -> StringsB.UC11_STEP_INTRO;
            case WHO -> StringsB.UC11_STEP_WHO;
            case DESCRIBE -> StringsB.UC11_STEP_DESCRIBE;
            case DATE -> StringsB.UC11_STEP_DATE;
            case CONFIRM -> StringsB.UC11_STEP_CONFIRM;
            case DONE -> StringsB.UC11_STEP_DONE;
        };
    }

    private String promptText(Step target) {
        return switch (target) {
            case INTRO -> confidentiality;
            case WHO -> StringsB.UC11_PROMPT_WHO;
            case DESCRIBE -> StringsB.UC11_PROMPT_DESCRIBE;
            case DATE -> StringsB.UC11_PROMPT_DATE;
            case CONFIRM -> StringsB.UC11_PROMPT_CONFIRM;
            case DONE -> StringsB.UC11_SUCCESS.formatted(controller.getReferenceNo());
        };
    }

    private Node body(Step target) {
        Node pane = switch (target) {
            case CONFIRM -> confirmation();
            case DONE -> referenceNumber();
            default -> inputPanes.get(target);
        };
        VBox holder = new VBox(pane);
        holder.setPadding(new Insets(8, 0, 8, 0));
        return holder;
    }

    private void goForward() {
        attempt(() -> {
            switch (step) {
                case INTRO -> {
                    controller.agreeToContinue(agree.isSelected());
                    loadEmployers();
                    show(Step.WHO);
                }
                case WHO -> {
                    HiringWoman picked = employer.getValue();
                    controller.enterEmployerAndProblemType(
                            picked == null ? null : picked.getHiringWomanId(), selectedType());
                    show(Step.DESCRIBE);
                }
                case DESCRIBE -> {
                    controller.describeIncident(audioPath, written.getText());
                    show(Step.DATE);
                }
                case DATE -> {
                    controller.enterDateAndEvidence(incidentDate.getValue(), evidencePath);
                    show(Step.CONFIRM);
                }
                case CONFIRM -> {
                    controller.confirmComplaint();
                    clearInputs();
                    show(Step.DONE);
                }
                case DONE -> navigator.goHome();
            }
        });
    }

    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showError(StringsB.UC11_ERR_DATABASE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UNEXPECTED_ERROR);
        }
    }

    private VBox confirmation() {
        GridPane summary = new GridPane();
        summary.setHgap(16);
        summary.setVgap(8);
        Label text = new Label(controller.getTranscript());
        text.setWrapText(true);
        text.setMaxWidth(520);
        int line = 0;
        summary.addRow(line++, new Label(StringsB.UC11_EMPLOYER), new Label(controller.getEmployerName()));
        summary.addRow(line++, new Label(StringsB.UC11_PROBLEM),
                new Label(StringsB.problemName(controller.getProblemType())));
        summary.addRow(line++, new Label(StringsB.UC11_TRANSCRIPT), text);
        summary.addRow(line++, new Label(StringsB.UC11_AUDIO), ltr(new Label(controller.getAudioName())));
        summary.addRow(line++, new Label(StringsB.UC11_INCIDENT_DATE),
                ltr(new Label(String.valueOf(controller.getIncidentDate()))));
        summary.addRow(line, new Label(StringsB.UC11_EVIDENCE), ltr(new Label(controller.getEvidenceName())));
        return new VBox(10, summary);
    }

    private VBox referenceNumber() {
        Label caption = new Label(StringsB.UC11_REF_LABEL);
        Label reference = ltr(new Label(controller.getReferenceNo()));
        reference.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        Label hint = new Label(StringsB.UC11_NEXT_STEPS);
        hint.setWrapText(true);
        VBox box = new VBox(12, caption, reference, hint);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Label ltr(Label label) {
        label.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        return label;
    }
}