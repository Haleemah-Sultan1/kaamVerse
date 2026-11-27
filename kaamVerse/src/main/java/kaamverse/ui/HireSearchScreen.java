package kaamverse.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import kaamverse.bl.controller.HiringController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.Locality;
import kaamverse.bl.model.Service;
import kaamverse.bl.model.Street;
import kaamverse.bl.service.HiringService.MaidListing;
import kaamverse.bl.service.HiringService.ServiceOffer;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;
import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

// UC3: filters and the list of verified maids. Talks to HiringController only.
public class HireSearchScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final HiringController controller = new HiringController();
    private final TableView<MaidListing> table = new TableView<>();
    private final ComboBox<Locality> localityBox = new ComboBox<>();
    private final ComboBox<Street> streetBox = new ComboBox<>();
    private final ComboBox<Service> serviceBox = new ComboBox<>();
    private final TextField ratingField = new TextField();
    private final ComboBox<WeekDay> dayBox = new ComboBox<>();
    private final ComboBox<TimeSlot> timeBox = new ComboBox<>();

    public HireSearchScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(StringsB.UC3_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        VBox top = new VBox(10, heading, buildFilters());
        setTop(top);

        buildTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(12, 0, 12, 0));

        Button view = new Button(StringsB.UC3_VIEW_PROFILE);
        view.setOnAction(event -> openProfile());
        Button myHires = new Button(StringsB.UC3_MY_HIRES);
        myHires.setOnAction(event -> navigator.show(new MyHiresScreen(navigator), Strings.APP_TITLE));
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        setBottom(new HBox(10, view, myHires, back));

        loadChoices();
        search();
    }

    private GridPane buildFilters() {
        localityBox.setConverter(names(Locality::getName));
        streetBox.setConverter(names(Street::getName));
        serviceBox.setConverter(names(Service::getName));
        for (ComboBox<?> box : List.of(localityBox, streetBox, serviceBox, dayBox, timeBox)) {
            box.setPromptText(StringsB.UC3_ANY);
            box.setPrefWidth(190);
        }
        ratingField.setPromptText("0");
        // choosing a locality loads its streets
        localityBox.valueProperty().addListener((observable, old, locality) -> {
            streetBox.setValue(null);
            streetBox.getItems().clear();
            if (locality != null) {
                loadStreets(locality.getLocalityId());
            }
        });

        Button search = new Button(StringsB.UC3_SEARCH);
        search.setOnAction(event -> search());
        Button clear = new Button(StringsB.UC3_CLEAR);
        clear.setOnAction(event -> clearFilters());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.addRow(0, new Label(StringsB.UC3_LOCALITY), localityBox, new Label(StringsB.UC3_STREET), streetBox);
        grid.addRow(1, new Label(StringsB.UC3_SERVICE), serviceBox, new Label(StringsB.UC3_MIN_RATING), ratingField);
        grid.addRow(2, new Label(StringsB.UC3_DAY), dayBox, new Label(StringsB.UC3_TIME), timeBox);
        grid.add(new HBox(10, search, clear), 1, 3);
        return grid;
    }

    private void buildTable() {
        table.getColumns().add(column(StringsB.UC3_COL_NAME, 150, row -> row.maid().getName()));
        table.getColumns().add(column(StringsB.UC3_COL_BADGE, 130,
                row -> row.maid().hasBadge() ? StringsB.UC3_BADGE : StringsB.UC3_NO_BADGE));
        table.getColumns().add(column(StringsB.UC3_COL_RATING, 100, row -> ratingText(row.maid().getAverageRating())));
        table.getColumns().add(column(StringsB.UC3_COL_AREA, 170, MaidListing::area));
        table.getColumns().add(column(StringsB.UC3_COL_SERVICES, 420, row -> offersText(row.offers())));
        table.setPlaceholder(new Label(StringsB.UC3_NO_RESULTS));
        table.setRowFactory(view -> {
            TableRow<MaidListing> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openProfile();
                }
            });
            return row;
        });
    }

    private void loadChoices() {
        try {
            localityBox.getItems().setAll(controller.getLocalities());
            serviceBox.getItems().setAll(controller.getServices());
            dayBox.getItems().setAll(WeekDay.values());
            timeBox.getItems().setAll(TimeSlot.values());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
    }

    private void loadStreets(int localityId) {
        try {
            streetBox.getItems().setAll(controller.getStreets(localityId));
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
    }

    private void search() {
        Locality locality = localityBox.getValue();
        Street street = streetBox.getValue();
        Service service = serviceBox.getValue();
        try {
            table.getItems().setAll(controller.searchMaids(
                    locality == null ? null : locality.getLocalityId(),
                    street == null ? null : street.getStreetId(),
                    service == null ? null : service.getServiceId(),
                    ratingField.getText(), dayBox.getValue(), timeBox.getValue()));
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
    }

    private void clearFilters() {
        localityBox.setValue(null);
        streetBox.setValue(null);
        serviceBox.setValue(null);
        dayBox.setValue(null);
        timeBox.setValue(null);
        ratingField.clear();
        search();
    }

    private void openProfile() {
        MaidListing row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            AlertHelper.showError(StringsB.UC3_ERR_SELECT);
            return;
        }
        // Back returns to this same screen, so the filters stay as they were
        navigator.show(new MaidProfileScreen(navigator, row.maid().getMaidId(),
                () -> navigator.show(this, Strings.APP_TITLE)), Strings.APP_TITLE);
    }

    private static String ratingText(double rating) {
        return rating <= 0 ? StringsB.UC3_NO_RATING : String.format("%.1f / 5", rating);
    }

    private static String offersText(List<ServiceOffer> offers) {
        List<String> parts = new ArrayList<>();
        for (ServiceOffer offer : offers) {
            parts.add(offer.service().getName() + ": " + (offer.band() == null ? StringsB.UC3_NO_BAND
                    : bandText(offer)));
        }
        return String.join("; ", parts);
    }

    static String bandText(ServiceOffer offer) {
        return String.format("Rs %,.0f - %,.0f %s", offer.band().getMinRate(), offer.band().getMaxRate(),
                StringsB.unitName(offer.band().getUnit()));
    }

    private static <T> StringConverter<T> names(Function<T, String> name) {
        return new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : name.apply(item);
            }

            @Override
            public T fromString(String text) {
                return null;
            }
        };
    }

    private static TableColumn<MaidListing, String> column(String title, double width,
                                                           Function<MaidListing, String> value) {
        TableColumn<MaidListing, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}