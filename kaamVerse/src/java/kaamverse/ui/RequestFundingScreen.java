package kaamverse.ui;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import kaamverse.bl.controller.FundingRequestController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.HiringWoman;
import kaamverse.common.DatabaseException;
import kaamverse.common.EventType;
import kaamverse.common.Strings;

/**
 * UC9: Urdu, right-to-left wizard for asking employers for event funding.
 * One step at a time in a StackPane: event type, amount and date, reason,
 * employers, confirmation, tracking number. The draft lives in the controller;
 * this class talks to FundingRequestController only (GRASP Low Coupling).
 */
public class RequestFundingScreen extends BorderPane {

    private enum Step { EVENT, AMOUNT, REASON, EMPLOYERS, CONFIRM, DONE }

    /** Steps the maid fills in; the tracking number screen is not counted. */
    private static final int COUNTED_STEPS = 5;

    private final SceneNavigator navigator;
    private final FundingRequestController controller = new FundingRequestController();

    // Inputs are kept as fields so that going back keeps what was entered.
    private final ToggleGroup eventGroup = new ToggleGroup();
    private final TextField amount = new TextField();
    private final DatePicker eventDate = new DatePicker();
    private final TextArea reason = new TextArea();
    private final Map<Integer, CheckBox> employerBoxes = new LinkedHashMap<>();
    private final VBox employerList = new VBox(10);
    private final Map<Step, Node> inputPanes = new EnumMap<>(Step.class);

    private final Label progress = new Label();
    private final Label heading = new Label();
    private final Label prompt = new Label();
    private final StackPane stack = new StackPane();
    private final ScrollPane content = new ScrollPane(stack);
    private final Button back = new Button(Strings.UC9_BACK);
    private final Button next = new Button();
    private final Button cancel = new Button(Strings.UC9_CANCEL);
    private Step step = Step.EVENT;
    /** What she typed, read back to her on the reason step. */
    private String readBack = "";

    public RequestFundingScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        setStyle(UrduFont.style());
        setPadding(new Insets(16, 24, 16, 24));

        // Eligibility: a maid who may not ask gets a message instead of the wizard.
        try {
            controller.startNewRequest();
        } catch (ValidationException e) {
            showBlocked(e.getMessage());
            return;
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UC9_ERR_DATABASE);
            showBlocked(Strings.UC9_ERR_DATABASE);
            return;
        }

        buildInputs();
        buildEmployerList();

        progress.getStyleClass().add("muted");
        heading.getStyleClass().add("screen-title");
        prompt.setWrapText(true);
        Button listen = new Button(Strings.UC9_LISTEN);
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
        show(Step.EVENT);
    }

    /** Shown instead of the wizard when she is not allowed to ask. */
    private void showBlocked(String message) {
        Label title = new Label(Strings.UC9_TITLE);
        title.getStyleClass().add("screen-title");
        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(600);
        Button listen = new Button(Strings.UC9_LISTEN);
        listen.setOnAction(event -> controller.playPrompt(message));
        Button home = new Button(Strings.UC9_BACK);
        home.setDefaultButton(true);
        home.setOnAction(event -> navigator.goHome());
        VBox box = new VBox(16, title, text, new HBox(10, home, listen));
        box.setAlignment(Pos.CENTER_LEFT);
        setCenter(box);
        controller.playPrompt(message);
    }

    // Inputs

    private void buildInputs() {
        FlowPane events = new FlowPane(16, 16);
        for (EventType type : EventType.values()) {
            ToggleButton choice = new ToggleButton(Strings.uc9EventName(type));
            choice.setUserData(type);
            choice.setToggleGroup(eventGroup);
            choice.setPrefSize(190, 70);
            choice.setStyle("-fx-font-size: 18px;");
            events.getChildren().add(choice);
        }
        inputPanes.put(Step.EVENT, events);

        // Numbers and dates read left to right even on a right-to-left screen.
        amount.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        amount.setPromptText("25000");
        amount.setPrefWidth(260);
        eventDate.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        eventDate.setEditable(false); // chosen from the calendar, so no free text to check
        eventDate.setPrefWidth(260);
        eventDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(!empty && date.isBefore(LocalDate.now()));
            }
        });
        GridPane amountForm = new GridPane();
        amountForm.setHgap(16);
        amountForm.setVgap(12);
        amountForm.addRow(0, new Label(Strings.UC9_AMOUNT), amount);
        amountForm.addRow(1, new Label(Strings.UC9_EVENT_DATE), eventDate);
        inputPanes.put(Step.AMOUNT, amountForm);

        reason.setWrapText(true);
        reason.setPrefRowCount(6);
        reason.setPrefWidth(560);
        reason.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= FundingRequestController.MAX_REASON_LENGTH ? change : null));
        inputPanes.put(Step.REASON, new VBox(8, new Label(Strings.UC9_REASON), reason));

        inputPanes.put(Step.EMPLOYERS, employerList);
    }

    /** Lists her current employers; keeps the ones already ticked that are still in the list. */
    private void buildEmployerList() {
        Set<Integer> ticked = tickedEmployerIds();
        employerBoxes.clear();
        employerList.getChildren().clear();
        for (HiringWoman employer : controller.getCurrentEmployers()) {
            CheckBox box = new CheckBox(employer.getName());
            box.setStyle("-fx-font-size: 16px;");
            box.setSelected(ticked.contains(employer.getHiringWomanId()));
            employerBoxes.put(employer.getHiringWomanId(), box);
            employerList.getChildren().add(box);
        }
    }

    private Set<Integer> tickedEmployerIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        employerBoxes.forEach((id, box) -> {
            if (box.isSelected()) {
                ids.add(id);
            }
        });
        return ids;
    }

    private EventType selectedEvent() {
        Toggle selected = eventGroup.getSelectedToggle();
        return selected == null ? null : (EventType) selected.getUserData();
    }

    // Steps

    private void show(Step target) {
        step = target;
        heading.setText(title(target));
        prompt.setText(promptText(target));
        progress.setText(target == Step.DONE ? ""
                : Strings.UC9_STEP_OF.formatted(target.ordinal() + 1, COUNTED_STEPS));
        stack.getChildren().setAll(body(target));
        content.setVvalue(0);

        boolean done = target == Step.DONE;
        back.setDisable(target == Step.EVENT);
        back.setVisible(!done);
        back.setManaged(!done);
        cancel.setVisible(!done);
        cancel.setManaged(!done);
        next.setText(switch (target) {
            case CONFIRM -> Strings.UC9_SUBMIT;
            case DONE -> Strings.UC9_FINISH;
            default -> Strings.UC9_NEXT;
        });
        controller.playPrompt(prompt.getText());
    }

    private static String title(Step target) {
        return switch (target) {
            case EVENT -> Strings.UC9_STEP_EVENT;
            case AMOUNT -> Strings.UC9_STEP_AMOUNT;
            case REASON -> Strings.UC9_STEP_REASON;
            case EMPLOYERS -> Strings.UC9_STEP_EMPLOYERS;
            case CONFIRM -> Strings.UC9_STEP_CONFIRM;
            case DONE -> Strings.UC9_STEP_DONE;
        };
    }

    private String promptText(Step target) {
        return switch (target) {
            case EVENT -> Strings.UC9_PROMPT_EVENT;
            case AMOUNT -> Strings.UC9_PROMPT_AMOUNT;
            case REASON -> readBack.isEmpty() ? Strings.UC9_PROMPT_REASON
                    : readBack + " " + Strings.UC9_PROMPT_REASON;
            case EMPLOYERS -> Strings.UC9_PROMPT_EMPLOYERS;
            case CONFIRM -> Strings.UC9_PROMPT_CONFIRM;
            case DONE -> Strings.UC9_SUCCESS.formatted(controller.getTrackingNo());
        };
    }

    private Node body(Step target) {
        Node pane = switch (target) {
            case CONFIRM -> confirmation();
            case DONE -> trackingNumber();
            default -> inputPanes.get(target);
        };
        VBox holder = new VBox(pane);
        holder.setPadding(new Insets(8, 0, 8, 0));
        return holder;
    }

    /** Validates the current step and, if it is correct, moves on. */
    private void goForward() {
        attempt(() -> {
            switch (step) {
                case EVENT -> {
                    controller.enterEventType(selectedEvent());
                    show(Step.AMOUNT);
                }
                case AMOUNT -> {
                    controller.enterAmountAndDate(amount.getText(), eventDate.getValue());
                    readBack = Strings.UC9_READBACK.formatted(controller.getAmountText(),
                            controller.getEventDate());
                    show(Step.REASON);
                }
                case REASON -> {
                    controller.enterReason(reason.getText());
                    show(Step.EMPLOYERS);
                }
                case EMPLOYERS -> {
                    controller.selectEmployers(tickedEmployerIds());
                    show(Step.CONFIRM);
                }
                case CONFIRM -> {
                    controller.submitRequest();
                    show(Step.DONE);
                }
                case DONE -> navigator.goHome();
            }
        });
    }

    /** Runs an action and turns each kind of failure into a friendly alert. */
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            if (step == Step.CONFIRM) {
                // Her employers may have changed since she chose; show the list as it is now.
                buildEmployerList();
                show(Step.CONFIRM);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UC9_ERR_DATABASE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UNEXPECTED_ERROR);
        }
    }

    // Confirmation and result

    private VBox confirmation() {
        GridPane summary = new GridPane();
        summary.setHgap(16);
        summary.setVgap(8);
        Label reasonText = new Label(controller.getReason());
        reasonText.setWrapText(true);
        reasonText.setMaxWidth(520);
        VBox employers = new VBox(4);
        for (HiringWoman employer : controller.getChosenEmployers()) {
            employers.getChildren().add(new Label(employer.getName()));
        }
        int line = 0;
        summary.addRow(line++, new Label(Strings.UC9_EVENT), new Label(Strings.uc9EventName(controller.getEventType())));
        summary.addRow(line++, new Label(Strings.UC9_AMOUNT), number(controller.getAmountText()));
        summary.addRow(line++, new Label(Strings.UC9_EVENT_DATE), number(String.valueOf(controller.getEventDate())));
        summary.addRow(line++, new Label(Strings.UC9_REASON), reasonText);
        summary.addRow(line, new Label(Strings.UC9_EMPLOYERS), employers);
        return new VBox(10, summary);
    }

    private VBox trackingNumber() {
        Label caption = new Label(Strings.UC9_TRACKING_LABEL);
        Label tracking = number(controller.getTrackingNo());
        tracking.getStyleClass().add("tracking-number");
        Label hint = new Label(Strings.UC9_DONE_HINT);
        hint.setWrapText(true);
        VBox box = new VBox(12, caption, tracking, hint);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Label number(String text) {
        Label label = new Label(text);
        label.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        return label;
    }
}
