package kaamverse.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.ComplaintController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.Complaint;
import kaamverse.bl.model.ReferralReport;
import kaamverse.bl.service.ComplaintReviewService.ComplaintCase;
import kaamverse.bl.service.ComplaintReviewService.ComplaintRow;
import kaamverse.bl.service.ComplaintReviewService.NoteLine;
import kaamverse.common.ComplaintOutcome;
import kaamverse.common.ComplaintStatus;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;

public class ReviewComplaintsScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final ComplaintController controller = new ComplaintController();

    private final TableView<ComplaintRow> table = new TableView<>();
    private final CheckBox showAll = new CheckBox(StringsB.UC12_SHOW_ALL);
    private final TextArea details = new TextArea();
    private final Button openAudio = new Button(StringsB.UC12_OPEN_AUDIO);
    private final Button openEvidence = new Button(StringsB.UC12_OPEN_EVIDENCE);
    private final TextArea noteArea = new TextArea();
    private final Button reviewButton = new Button(StringsB.UC12_START_REVIEW);
    private final TextArea clarifyArea = new TextArea();
    private final Button clarifyButton = new Button(StringsB.UC12_REQUEST_CLARIFICATION);
    private final ComboBox<ComplaintOutcome> outcomeBox = new ComboBox<>();
    private final Button decideButton = new Button(StringsB.UC12_DECIDE);
    private final ComboBox<String> authorityBox = new ComboBox<>();
    private final TextArea reasonArea = new TextArea();
    private final Button escalateButton = new Button(StringsB.UC12_ESCALATE);
    private final VBox escalationBox = new VBox(8);
    private ComplaintCase current;

    public ReviewComplaintsScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(StringsB.UC12_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        showAll.setOnAction(event -> loadList(null));
        setTop(new VBox(8, heading, showAll));

        buildTable();
        buildActions();
        VBox content = new VBox(12, table, details, new HBox(10, openAudio, openEvidence),
                section(StringsB.UC12_NOTE_LABEL, noteArea, reviewButton),
                section(StringsB.UC12_CLARIFY_LABEL, clarifyArea, clarifyButton),
                outcomeRow(), escalationBox);
        content.setPadding(new Insets(10, 12, 10, 0));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        setCenter(scroll);
        BorderPane.setMargin(scroll, new Insets(10, 0, 10, 0));

        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        setBottom(back);

        showCase(null);
        loadList(null);
    }

    private void buildTable() {
        table.getColumns().add(column(StringsB.UC12_COL_REF, 120, row -> row.complaint().getReferenceNo()));
        table.getColumns().add(column(StringsB.UC12_COL_MAID, 130, ComplaintRow::maidName));
        table.getColumns().add(column(StringsB.UC12_COL_EMPLOYER, 130, ComplaintRow::employerName));
        table.getColumns().add(column(StringsB.UC12_COL_TYPE, 170, row -> row.complaint().getProblemType()));
        table.getColumns().add(column(StringsB.UC12_COL_URGENCY, 70, row -> row.complaint().getUrgency()));
        table.getColumns().add(column(StringsB.UC12_COL_STATUS, 100, row -> row.complaint().getStatus().name()));
        table.getColumns().add(column(StringsB.UC12_COL_FILED, 90, row -> String.valueOf(row.complaint().getFiledDate())));
        table.setPlaceholder(new Label(StringsB.UC12_NO_COMPLAINTS));
        table.setPrefHeight(200);
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, row) -> openRow(row));

        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(12);
    }

    private void buildActions() {
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(3);
        clarifyArea.setWrapText(true);
        clarifyArea.setPrefRowCount(3);
        outcomeBox.getItems().setAll(ComplaintOutcome.values());
        outcomeBox.setPrefWidth(260);
        reasonArea.setWrapText(true);
        reasonArea.setPrefRowCount(3);
        authorityBox.setPrefWidth(260);

        openAudio.setOnAction(event -> openFile(current.complaint().getAudioPath()));
        openEvidence.setOnAction(event -> openFile(current.complaint().getEvidencePath()));
        reviewButton.setOnAction(event -> startReview());
        clarifyButton.setOnAction(event -> requestClarification());
        decideButton.setOnAction(event -> decide());
        escalateButton.setOnAction(event -> escalate());

        Label escalationTitle = new Label(StringsB.UC12_ESCALATION);
        escalationTitle.setStyle("-fx-font-weight: bold;");
        escalationBox.getChildren().addAll(escalationTitle, new Label(StringsB.UC12_AUTHORITY), authorityBox,
                new Label(StringsB.UC12_REASON), reasonArea, escalateButton);
    }

    private VBox section(String title, TextArea area, Button button) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");
        return new VBox(6, label, area, button);
    }

    private VBox outcomeRow() {
        Label label = new Label(StringsB.UC12_OUTCOME_LABEL);
        label.setStyle("-fx-font-weight: bold;");
        return new VBox(6, label, new HBox(10, outcomeBox, decideButton));
    }

    private static <S> TableColumn<S, String> column(String title, double width, Function<S, String> value) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void loadList(Integer keepId) {
        attempt(() -> {
            table.getItems().setAll(controller.selectComplaints(showAll.isSelected()));
            for (ComplaintRow row : table.getItems()) {
                if (keepId != null && row.complaint().getComplaintId() == keepId) {
                    table.getSelectionModel().select(row);
                    return;
                }
            }
            showCase(null);
        });
    }

    private void openRow(ComplaintRow row) {
        if (row == null) {
            showCase(null);
            return;
        }
        attempt(() -> showCase(controller.openComplaint(row.complaint().getComplaintId())));
    }

    private void showCase(ComplaintCase item) {
        current = item;
        escalationBox.setVisible(false);
        escalationBox.setManaged(false);
        authorityBox.getItems().clear();
        reasonArea.clear();
        outcomeBox.setValue(null);
        if (item == null) {
            details.clear();
            enableActions(false, false, false, false);
            return;
        }
        details.setText(describe(item));
        ComplaintStatus status = item.complaint().getStatus();
        boolean canReview = status == ComplaintStatus.Filed || status == ComplaintStatus.UnderReview;
        boolean underReview = status == ComplaintStatus.UnderReview;
        enableActions(canReview, underReview, item.complaint().getAudioPath() != null,
                item.complaint().getEvidencePath() != null);
    }

    private void enableActions(boolean canReview, boolean underReview, boolean hasAudio, boolean hasEvidence) {
        noteArea.setDisable(!canReview);
        reviewButton.setDisable(!canReview);
        clarifyArea.setDisable(!underReview);
        clarifyButton.setDisable(!underReview);
        outcomeBox.setDisable(!underReview);
        decideButton.setDisable(!underReview);
        openAudio.setDisable(!hasAudio);
        openEvidence.setDisable(!hasEvidence);
    }

    private static String describe(ComplaintCase item) {
        Complaint complaint = item.complaint();
        StringBuilder text = new StringBuilder();
        line(text, StringsB.UC12_F_REFERENCE, complaint.getReferenceNo());
        line(text, StringsB.UC12_F_STATUS, complaint.getStatus().name());
        line(text, StringsB.UC12_F_URGENCY, complaint.getUrgency());
        line(text, StringsB.UC12_F_TYPE, complaint.getProblemType());
        line(text, StringsB.UC12_F_MAID, item.maidName());
        line(text, StringsB.UC12_F_EMPLOYER, item.employerName());
        line(text, StringsB.UC12_F_INCIDENT, String.valueOf(complaint.getIncidentDate()));
        line(text, StringsB.UC12_F_FILED, String.valueOf(complaint.getFiledDate()));
        line(text, StringsB.UC12_F_AUDIO, complaint.getAudioPath());
        line(text, StringsB.UC12_F_EVIDENCE, complaint.getEvidencePath());
        line(text, StringsB.UC12_F_CLARIFICATION, complaint.getClarificationMessage());
        line(text, StringsB.UC12_F_OUTCOME, complaint.getOutcome());
        item.referral().ifPresent(report -> line(text, StringsB.UC12_F_REFERRAL,
                report.getAuthority() + " - " + report.getReason()));
        text.append('\n').append(StringsB.UC12_F_TRANSCRIPT).append(":\n").append(complaint.getTranscript()).append('\n');
        text.append('\n').append(StringsB.UC12_F_NOTES).append(":\n");
        if (item.notes().isEmpty()) {
            text.append(StringsB.UC12_NONE).append('\n');
        }
        for (NoteLine line : item.notes()) {
            text.append(line.note().getNoteDate()).append("  ").append(line.officerName()).append(": ")
                    .append(line.note().getText()).append('\n');
        }
        return text.toString();
    }

    private static void line(StringBuilder text, String label, String value) {
        text.append(label).append(": ").append(value == null || value.isBlank() ? StringsB.UC12_NONE : value).append('\n');
    }

    private void startReview() {
        int id = selectedId();
        if (id < 0) {
            return;
        }
        String note = noteArea.getText();
        if (attempt(() -> controller.setStatusUnderReview(id, note))) {
            noteArea.clear();
            AlertHelper.showInfo(StringsB.UC12_REVIEW_SAVED);
        }
        loadList(id);
    }

    private void requestClarification() {
        int id = selectedId();
        if (id < 0) {
            return;
        }
        String message = clarifyArea.getText();
        if (attempt(() -> controller.requestClarification(id, message))) {
            clarifyArea.clear();
            AlertHelper.showInfo(StringsB.UC12_CLARIFICATION_SENT);
        }
        loadList(id);
    }

    private void decide() {
        int id = selectedId();
        ComplaintOutcome outcome = outcomeBox.getValue();
        if (id < 0) {
            return;
        }
        if (outcome != null && !outcome.isEscalation() && !AlertHelper.confirm(
                StringsB.UC12_CONFIRM_CLOSE.formatted(current.complaint().getReferenceNo(), outcome.getLabel()))) {
            return;
        }
        List<String> authorities = new ArrayList<>();
        if (!attempt(() -> authorities.addAll(controller.decideOutcome(id, outcome)))) {
            loadList(id);
            return;
        }
        if (outcome.isEscalation()) {
            authorityBox.getItems().setAll(authorities);
            escalationBox.setVisible(true);
            escalationBox.setManaged(true);
            AlertHelper.showInfo(StringsB.UC12_CHOOSE_AUTHORITY);
            return;
        }
        AlertHelper.showInfo(StringsB.UC12_CLOSED);
        loadList(id);
    }

    private void escalate() {
        int id = selectedId();
        if (id < 0) {
            return;
        }
        String authority = authorityBox.getValue();
        String reason = reasonArea.getText();
        if (authority != null && !AlertHelper.confirm(StringsB.UC12_CONFIRM_ESCALATE.formatted(authority))) {
            return;
        }
        boolean incomplete = authority == null || reason == null || reason.isBlank();
        ReferralReport[] report = new ReferralReport[1];
        if (attempt(() -> report[0] = controller.escalateComplaint(id, authority, reason))) {
            AlertHelper.showInfo(StringsB.UC12_ESCALATED.formatted(authority, report[0].getReferralId()));
        }
        // a form that was left incomplete keeps what was typed
        if (!incomplete) {
            loadList(id);
        }
    }

    private int selectedId() {
        ComplaintRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            AlertHelper.showError(StringsB.UC12_ERR_SELECT);
            return -1;
        }
        return row.complaint().getComplaintId();
    }

    private void openFile(String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            AlertHelper.showError(StringsB.UC12_ERR_FILE_MISSING.formatted(path));
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            AlertHelper.showInfo(file.getAbsolutePath());
            return;
        }
        // opening can block, so it runs off the screen thread
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException | RuntimeException e) {
                Platform.runLater(() -> AlertHelper.showError(StringsB.UC12_ERR_FILE_OPEN.formatted(path)));
            }
        }).start();
    }

    private boolean attempt(Runnable action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        }
        return false;
    }
}