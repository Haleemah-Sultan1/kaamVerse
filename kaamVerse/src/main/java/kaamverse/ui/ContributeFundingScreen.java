package kaamverse.ui;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.ContributionController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.ContributionReceipt;
import kaamverse.bl.model.FundingCase;
import kaamverse.bl.model.Maid;
import kaamverse.bl.service.FundingRequestService;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;

/**
 * UC10: the requests sent to the logged-in hiring woman, one request in
 * detail, then the amount and note, a confirmation and the acknowledgement.
 * Talks to ContributionController only (GRASP Low Coupling).
 */
public class ContributeFundingScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final ContributionController controller = new ContributionController();
    private final TableView<FundingCase> table = new TableView<>();

    public ContributeFundingScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));
        buildTable();
        showList();
    }

    // List of requests

    private void buildTable() {
        table.getColumns().add(column(Strings.UC10_COL_MAID, 200, item -> item.maid().getName()));
        table.getColumns().add(column(Strings.UC10_COL_EVENT, 160, item -> item.request().getEventType()));
        table.getColumns().add(column(Strings.UC10_COL_NEEDED, 130,
                item -> amount(item.request().getAmountNeeded())));
        table.getColumns().add(column(Strings.UC10_COL_RECEIVED, 130,
                item -> amount(item.request().getAmountReceived())));
        table.setPlaceholder(new Label(Strings.UC10_NO_REQUESTS));
        table.setRowFactory(view -> {
            TableRow<FundingCase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private static TableColumn<FundingCase, String> column(String title, double width,
            Function<FundingCase, String> value) {
        TableColumn<FundingCase, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void showList() {
        Label heading = new Label(Strings.UC10_CONTRIBUTE);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button open = new Button(Strings.UC10_OPEN);
        open.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        open.setOnAction(event -> openDetail(table.getSelectionModel().getSelectedItem()));
        Button refresh = new Button(Strings.REFRESH);
        refresh.setOnAction(event -> loadRequests());
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        HBox buttons = new HBox(10, open, refresh, back);
        buttons.setPadding(new Insets(12, 0, 0, 0));

        setTop(heading);
        setCenter(table);
        setBottom(buttons);
        BorderPane.setMargin(table, new Insets(12, 0, 0, 0));
        loadRequests();
    }

    private void loadRequests() {
        attempt(() -> table.getItems().setAll(controller.getSupportRequests()));
    }

    private void openDetail(FundingCase selected) {
        if (selected == null) {
            return;
        }
        FundingCase[] fresh = new FundingCase[1];
        if (attempt(() -> fresh[0] = controller.openRequest(selected.request().getRequestId()))) {
            showDetail(fresh[0]);
        } else {
            loadRequests(); // it may have been funded meanwhile
        }
    }

    // One request in detail

    private void showDetail(FundingCase item) {
        Maid maid = item.maid();
        Label heading = new Label(Strings.UC10_DETAIL_HEADING.formatted(maid.getName()));
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(6);
        int line = 0;
        details.addRow(line++, bold(Strings.UC10_FIELD_MAID), value(maid.getName()));
        details.addRow(line++, bold(Strings.UC10_FIELD_EVENT), value(item.request().getEventType()));
        details.addRow(line++, bold(Strings.UC10_FIELD_REASON), value(item.request().getReason()));
        details.addRow(line++, bold(Strings.UC10_FIELD_NEEDED), value(amount(item.request().getAmountNeeded())));
        details.addRow(line++, bold(Strings.UC10_FIELD_RECEIVED), value(amount(item.request().getAmountReceived())));
        details.addRow(line++, bold(Strings.UC10_FIELD_REMAINING), value(amount(item.request().getRemainingAmount())));
        details.addRow(line++, bold(Strings.UC10_FIELD_BADGE),
                value(maid.hasBadge() ? Strings.UC10_YES : Strings.UC10_NO));
        details.addRow(line, bold(Strings.UC10_FIELD_SERVICE),
                value(Strings.uc10LengthOfService(maid.getLengthOfService())));

        TextField amount = new TextField();
        amount.setPromptText("2500");
        amount.setPrefWidth(200);
        TextArea note = new TextArea();
        note.setPromptText(Strings.UC10_NOTE_LABEL);
        note.setWrapText(true);
        note.setPrefRowCount(3);
        note.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= Strings.UC10_MAX_NOTE ? change : null));

        Button contribute = new Button(Strings.UC10_BUTTON_CONTRIBUTE);
        contribute.setDefaultButton(true);
        contribute.setOnAction(event -> contribute(item, amount.getText(), note.getText()));
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> showList());

        VBox body = new VBox(12, details, bold(Strings.UC10_AMOUNT_LABEL), amount,
                bold(Strings.UC10_NOTE_LABEL), note, new HBox(10, contribute, back));
        body.setPadding(new Insets(12, 0, 0, 0));
        body.setMaxWidth(560);

        setTop(heading);
        setCenter(body);
        setBottom(null);
    }

    /** Checks the input, asks her to confirm, saves, and shows the acknowledgement. */
    private void contribute(FundingCase item, String amountText, String noteText) {
        if (!attempt(() -> controller.enterContribution(amountText, noteText))) {
            return;
        }
        String question = Strings.UC10_CONFIRM.formatted(controller.getAmountText(),
                item.maid().getName(), item.request().getEventType());
        if (!AlertHelper.confirm(question)) {
            return;
        }
        if (attempt(controller::confirmContribution)) {
            showAcknowledgement(controller.getReceipt());
        } else {
            refreshAfterFailure(item);
        }
    }

    /** The request may have changed or closed meanwhile: show it as it is now. */
    private void refreshAfterFailure(FundingCase item) {
        FundingCase[] fresh = new FundingCase[1];
        if (attempt(() -> fresh[0] = controller.openRequest(item.request().getRequestId()))) {
            showDetail(fresh[0]);
        } else {
            showList();
        }
    }

    // Acknowledgement

    private void showAcknowledgement(ContributionReceipt receipt) {
        Label heading = new Label(Strings.UC10_ACK_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label caption = new Label(Strings.UC10_ACK_NUMBER);
        Label number = new Label(receipt.contribution().getAcknowledgementNo());
        number.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        Label summary = new Label(Strings.UC10_ACK_TEXT.formatted(
                amount(receipt.contribution().getAmount()),
                amount(receipt.request().getAmountReceived()),
                amount(receipt.request().getAmountNeeded()),
                Strings.uc10StatusName(receipt.request().getStatus())));
        summary.setWrapText(true);

        Button list = new Button(Strings.UC10_ACK_BACK);
        list.setDefaultButton(true);
        list.setOnAction(event -> showList());
        Button home = new Button(Strings.BACK);
        home.setOnAction(event -> navigator.goHome());

        VBox box = new VBox(12, caption, number, summary, new HBox(10, list, home));
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(12, 0, 0, 0));
        setTop(heading);
        setCenter(box);
        setBottom(null);
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

    private static String amount(double value) {
        return FundingRequestService.formatAmount(value);
    }

    private static Label bold(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private static Label value(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.setAlignment(Pos.TOP_LEFT);
        return label;
    }
}
