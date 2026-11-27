package kaamverse.ui;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import kaamverse.bl.controller.HiringController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.service.HiringService.HireRow;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;

// UC3 extension: the hires of the logged-in woman, with Activate and End.
public class MyHiresScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final HiringController controller = new HiringController();
    private final TableView<HireRow> table = new TableView<>();

    public MyHiresScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(StringsB.UC3_MY_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        setTop(heading);

        buildTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(12, 0, 12, 0));

        Button activate = new Button(StringsB.UC3_ACTIVATE);
        activate.setOnAction(event -> activate());
        Button end = new Button(StringsB.UC3_END);
        end.setOnAction(event -> end());
        Button find = new Button(StringsB.UC3_FIND);
        find.setOnAction(event -> navigator.show(new HireSearchScreen(navigator), Strings.APP_TITLE));
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        setBottom(new HBox(10, activate, end, find, back));

        load();
    }

    private void buildTable() {
        table.getColumns().add(column(StringsB.UC3_MY_COL_MAID, 170, HireRow::maidName));
        table.getColumns().add(column(StringsB.UC3_MY_COL_HOURS, 150, row -> row.hire().getWorkingHours()));
        table.getColumns().add(column(StringsB.UC3_MY_COL_REQUESTED, 100, row -> text(row.hire().getRequestDate())));
        table.getColumns().add(column(StringsB.UC3_MY_COL_STARTED, 100, row -> text(row.hire().getStartDate())));
        table.getColumns().add(column(StringsB.UC3_MY_COL_ENDED, 100, row -> text(row.hire().getEndDate())));
        table.getColumns().add(column(StringsB.UC3_MY_COL_STATUS, 90, row -> row.hire().getStatus().name()));
        table.getColumns().add(column(StringsB.UC3_MY_COL_WAGE, 110,
                row -> String.format("%,.2f", row.hire().getCalculatedWage())));
        table.setPlaceholder(new Label(StringsB.UC3_MY_NONE));
    }

    private void load() {
        try {
            table.getItems().setAll(controller.getMyHires());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
    }

    private void activate() {
        HireRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            AlertHelper.showError(StringsB.UC3_ERR_SELECT_HIRE);
            return;
        }
        try {
            controller.activateHire(row.hire().getHireId());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            return;
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
            return;
        }
        load();
    }

    private void end() {
        HireRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            AlertHelper.showError(StringsB.UC3_ERR_SELECT_HIRE);
            return;
        }
        if (!AlertHelper.confirm(StringsB.UC3_END_ASK)) {
            return;
        }
        try {
            controller.endHire(row.hire().getHireId());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            return;
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
            return;
        }
        load();
    }

    private static String text(Object value) {
        return value == null ? "-" : value.toString();
    }

    private static TableColumn<HireRow, String> column(String title, double width, Function<HireRow, String> value) {
        TableColumn<HireRow, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}