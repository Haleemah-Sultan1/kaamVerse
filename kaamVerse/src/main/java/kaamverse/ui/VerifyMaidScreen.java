package kaamverse.ui;

import java.nio.file.Path;
import java.util.Optional;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.controller.VerificationController;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.VerificationCase;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;

/**
 * UC2: pending verification requests, then one request in detail to mark
 * Matched or Rejected. Talks to VerificationController only (GRASP Low Coupling).
 */
public class VerifyMaidScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final VerificationController controller = new VerificationController();
    private final TableView<VerificationCase> table = new TableView<>();

    public VerifyMaidScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));
        buildTable();
        showList();
    }

    // List of pending requests

    private void buildTable() {
        table.getColumns().add(column(Strings.UC2_COL_REQUEST, 90,
                item -> String.valueOf(item.request().getRequestId())));
        table.getColumns().add(column(Strings.UC2_COL_NAME, 220, item -> item.maid().getName()));
        table.getColumns().add(column(Strings.UC2_COL_CNIC, 160, item -> item.maid().getCnic()));
        table.getColumns().add(column(Strings.UC2_COL_SUBMITTED, 120,
                item -> String.valueOf(item.request().getSubmittedDate())));
        table.setPlaceholder(new Label(Strings.UC2_NO_PENDING));
        table.setRowFactory(view -> {
            TableRow<VerificationCase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private static TableColumn<VerificationCase, String> column(String title, double width,
            java.util.function.Function<VerificationCase, String> value) {
        TableColumn<VerificationCase, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void showList() {
        Label heading = new Label(Strings.UC2_VERIFY);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button review = new Button(Strings.UC2_REVIEW);
        review.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        review.setOnAction(event -> openDetail(table.getSelectionModel().getSelectedItem()));
        Button refresh = new Button(Strings.REFRESH);
        refresh.setOnAction(event -> loadPending());
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        HBox buttons = new HBox(10, review, refresh, back);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        setTop(heading);
        setCenter(table);
        setBottom(buttons);
        BorderPane.setMargin(table, new Insets(12, 0, 0, 0));
        loadPending();
    }

    private void loadPending() {
        attempt(() -> table.getItems().setAll(controller.getPendingRequests()));
    }

    private void openDetail(VerificationCase selected) {
        if (selected == null) {
            return;
        }
        // Reloaded, so the officer sees the latest details and a decided request is refused.
        VerificationCase[] fresh = new VerificationCase[1];
        if (attempt(() -> fresh[0] = controller.selectRequest(selected.request().getRequestId()))) {
            showDetail(fresh[0]);
        }
    }

    // One request in detail

    private void showDetail(VerificationCase item) {
        Maid maid = item.maid();
        int requestId = item.request().getRequestId();

        Label heading = new Label(Strings.UC2_DETAIL_HEADING.formatted(maid.getName()));
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(6);
        int line = 0;
        details.addRow(line++, bold(Strings.UC2_FIELD_NAME), value(maid.getName()));
        details.addRow(line++, bold(Strings.UC2_FIELD_FATHER), value(maid.getFatherName()));
        details.addRow(line++, bold(Strings.UC2_FIELD_CNIC), value(maid.getCnic()));
        details.addRow(line++, bold(Strings.UC2_FIELD_ADDRESS), value(maid.getAddress()));
        details.addRow(line++, bold(Strings.UC2_FIELD_SUBMITTED), value(String.valueOf(item.request().getSubmittedDate())));

        TextArea reason = new TextArea();
        reason.setPromptText(Strings.UC2_REASON_LABEL);
        reason.setWrapText(true);
        reason.setPrefRowCount(3);

        Button matched = new Button(Strings.UC2_MATCHED);
        matched.setOnAction(event -> {
            if (AlertHelper.confirm(Strings.UC2_CONFIRM_MATCH.formatted(maid.getName()))) {
                complete(() -> controller.markMatched(requestId),
                        Strings.UC2_MATCHED_DONE.formatted(maid.getName()));
            }
        });
        Button rejected = new Button(Strings.UC2_REJECTED);
        rejected.setOnAction(event -> {
            // Checked first so that the officer is not asked to confirm an incomplete rejection.
            if (!attempt(() -> controller.validateRejectionReason(reason.getText()))) {
                reason.requestFocus();
                return;
            }
            if (AlertHelper.confirm(Strings.UC2_CONFIRM_REJECT.formatted(maid.getName()))) {
                complete(() -> controller.reject(requestId, reason.getText()),
                        Strings.UC2_REJECTED_DONE.formatted(maid.getName()));
            }
        });
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> showList());
        HBox buttons = new HBox(10, matched, rejected, back);

        VBox left = new VBox(12, details, bold(Strings.UC2_REASON_LABEL), reason, buttons);
        left.setPrefWidth(380);
        VBox right = new VBox(8, bold(Strings.UC2_CNIC_IMAGE), cnicImage(item));
        HBox body = new HBox(32, left, right);
        body.setPadding(new Insets(12, 0, 0, 0));

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        setTop(heading);
        setCenter(scroll);
        setBottom(null);
    }

    /** The CNIC picture, or a message if the file is missing or cannot be read. */
    private javafx.scene.Node cnicImage(VerificationCase item) {
        Optional<Path> file = Optional.empty();
        try {
            file = controller.getCnicImage(item);
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        }
        if (file.isPresent()) {
            Image image = new Image(file.get().toUri().toString());
            if (!image.isError()) {
                ImageView view = new ImageView(image);
                view.setFitWidth(440);
                view.setFitHeight(320);
                view.setPreserveRatio(true);
                return view;
            }
        }
        Label missing = new Label(Strings.UC2_NO_IMAGE);
        missing.setWrapText(true);
        return missing;
    }

    /** Runs the decision; on success tells the officer and returns to the list. */
    private void complete(Runnable decision, String successMessage) {
        if (attempt(decision)) {
            AlertHelper.showInfo(successMessage);
            showList();
        }
    }

    /** Runs an action and turns each kind of failure into a friendly alert. */
    private boolean attempt(Runnable action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        } catch (RuntimeException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UNEXPECTED_ERROR);
        }
        return false;
    }

    private static Label bold(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private static Label value(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(240);
        label.setAlignment(Pos.TOP_LEFT);
        return label;
    }
}
