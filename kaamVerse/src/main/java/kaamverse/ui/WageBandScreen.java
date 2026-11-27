package kaamverse.ui;

import java.time.LocalDate;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.controller.WageController;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.WageBand;
import kaamverse.bl.service.WageService.BandRow;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;
import kaamverse.common.WageUnit;

/**
 * UC6: the current wage bands, and a form to set a new band for a service.
 * Talks to WageController only (GRASP Low Coupling).
 */
public class WageBandScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final WageController controller = new WageController();
    private final TableView<BandRow> table = new TableView<>();
    private final ComboBox<Service> serviceBox = new ComboBox<>();
    private final TextField minField = new TextField();
    private final TextField maxField = new TextField();
    private final ComboBox<WageUnit> unitBox = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());

    public WageBandScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(StringsB.UC6_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        setTop(heading);

        buildTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(12, 0, 12, 0));

        setBottom(buildForm());
        loadAll();
    }

    private void buildTable() {
        table.getColumns().add(column(StringsB.UC6_COL_SERVICE, 160, row -> row.service().getName()));
        table.getColumns().add(column(StringsB.UC6_COL_MIN, 110, row -> money(row.band().getMinRate())));
        table.getColumns().add(column(StringsB.UC6_COL_MAX, 110, row -> money(row.band().getMaxRate())));
        table.getColumns().add(column(StringsB.UC6_COL_UNIT, 110,
                row -> StringsB.unitName(row.band().getUnit())));
        table.getColumns().add(column(StringsB.UC6_COL_EFFECTIVE, 130,
                row -> String.valueOf(row.band().getEffectiveDate())));
        table.getColumns().add(column(StringsB.UC6_COL_STATUS, 110,
                row -> row.current() ? StringsB.UC6_STATUS_CURRENT : StringsB.UC6_STATUS_SCHEDULED));
        table.setPlaceholder(new Label(StringsB.UC6_NO_BANDS));
        // Selecting a row copies its values into the form, ready to edit.
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, row) -> {
            if (row != null) {
                copyToForm(row);
            }
        });
    }

    private static TableColumn<BandRow, String> column(String title, double width,
                                                       Function<BandRow, String> value) {
        TableColumn<BandRow, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private VBox buildForm() {
        Label formHeading = new Label(StringsB.UC6_FORM_HEADING);
        formHeading.setStyle("-fx-font-weight: bold;");

        serviceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Service service) {
                return service == null ? "" : service.getName();
            }

            @Override
            public Service fromString(String text) {
                return null;
            }
        });
        unitBox.getItems().setAll(WageUnit.values());
        unitBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(WageUnit unit) {
                return unit == null ? "" : StringsB.unitName(unit);
            }

            @Override
            public WageUnit fromString(String text) {
                return null;
            }
        });
        // Past dates cannot be picked; the controller checks typed dates too.
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(8);
        form.addRow(0, new Label(StringsB.UC6_SERVICE), serviceBox, new Label(StringsB.UC6_UNIT), unitBox);
        form.addRow(1, new Label(StringsB.UC6_MIN), minField, new Label(StringsB.UC6_MAX), maxField);
        form.addRow(2, new Label(StringsB.UC6_EFFECTIVE), datePicker);

        Button save = new Button(StringsB.UC6_SAVE);
        save.setOnAction(event -> save());
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        HBox buttons = new HBox(10, save, back);

        VBox box = new VBox(10, formHeading, form, buttons);
        box.setPadding(new Insets(4, 0, 0, 0));
        return box;
    }

    private void loadAll() {
        try {
            serviceBox.getItems().setAll(controller.getServices());
            table.getItems().setAll(controller.getCurrentBands());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
    }

    private void copyToForm(BandRow row) {
        WageBand band = row.band();
        for (Service service : serviceBox.getItems()) {
            if (service.getServiceId() == band.getServiceId()) {
                serviceBox.setValue(service);
            }
        }
        unitBox.setValue(band.getUnit());
        minField.setText(plain(band.getMinRate()));
        maxField.setText(plain(band.getMaxRate()));
        datePicker.setValue(LocalDate.now());
    }

    private void save() {
        try {
            controller.submitWageBand(serviceBox.getValue(), minField.getText(), maxField.getText(),
                    unitBox.getValue(), datePicker.getValue());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            return;
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
            return;
        }
        AlertHelper.showInfo(StringsB.UC6_SAVED);
        table.getSelectionModel().clearSelection();
        serviceBox.setValue(null);
        unitBox.setValue(null);
        minField.clear();
        maxField.clear();
        datePicker.setValue(LocalDate.now());
        loadAll();
    }

    private static String money(double amount) {
        return String.format("%,.2f", amount);
    }

    private static String plain(double amount) {
        return String.format("%.2f", amount);
    }
}