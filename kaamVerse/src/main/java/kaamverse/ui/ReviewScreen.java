package kaamverse.ui;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.ReviewController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.service.ExperienceService.PastHire;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;

// UC4: pick an ended hire, rate it with stars, add a comment. Talks to ReviewController only.
public class ReviewScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final ReviewController controller = new ReviewController();
    private final TableView<PastHire> table = new TableView<>();
    private final Label formHeading = new Label();
    private final StarRating overall = new StarRating();
    private final StarRating punctuality = new StarRating();
    private final StarRating honesty = new StarRating();
    private final StarRating cleanliness = new StarRating();
    private final StarRating behaviour = new StarRating();
    private final CheckBox rehire = new CheckBox(StringsB.UC4_WOULD_REHIRE);
    private final TextArea comment = new TextArea();
    private final Button submit = new Button(StringsB.UC4_SUBMIT);
    private final VBox form = new VBox(10);

    public ReviewScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(StringsB.UC4_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label listHeading = new Label(StringsB.UC4_LIST_HEADING);
        listHeading.setStyle("-fx-font-weight: bold;");
        buildTable();
        table.setPrefHeight(170);
        buildForm();

        VBox center = new VBox(10, heading, listHeading, table, form);
        VBox.setVgrow(form, Priority.ALWAYS);
        setCenter(center);

        submit.setOnAction(event -> submit());
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        setBottom(new HBox(10, submit, back));
        BorderPane.setMargin(center, new Insets(0, 0, 12, 0));

        load();
        updateForm(null);
    }

    private void buildTable() {
        table.getColumns().add(column(StringsB.UC4_COL_MAID, 200, PastHire::maidName));
        table.getColumns().add(column(StringsB.UC4_COL_STARTED, 130, item -> String.valueOf(item.hire().getStartDate())));
        table.getColumns().add(column(StringsB.UC4_COL_ENDED, 130, item -> String.valueOf(item.hire().getEndDate())));
        table.getColumns().add(column(StringsB.UC4_COL_REVIEW, 110,
                item -> item.reviewed() ? StringsB.UC4_REVIEWED : StringsB.UC4_NOT_REVIEWED));
        table.setPlaceholder(new Label(StringsB.UC4_NO_HIRES));
        table.getSelectionModel().selectedItemProperty().addListener((observable, old, item) -> updateForm(item));
    }

    private void buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(4);
        grid.addRow(0, new Label(StringsB.UC4_OVERALL), overall);
        grid.addRow(1, new Label(StringsB.UC4_PUNCTUALITY), punctuality);
        grid.addRow(2, new Label(StringsB.UC4_HONESTY), honesty);
        grid.addRow(3, new Label(StringsB.UC4_CLEANLINESS), cleanliness);
        grid.addRow(4, new Label(StringsB.UC4_BEHAVIOUR), behaviour);
        formHeading.setStyle("-fx-font-weight: bold;");
        comment.setPrefRowCount(3);
        comment.setWrapText(true);
        form.getChildren().addAll(formHeading, grid, rehire, new Label(StringsB.UC4_COMMENT), comment);
    }

    private void load() {
        try {
            table.getItems().setAll(controller.getPastMaids());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
    }

    // the form is usable only for a selected hire that has no review yet
    private void updateForm(PastHire item) {
        boolean usable = item != null && !item.reviewed();
        form.setDisable(!usable);
        submit.setDisable(!usable);
        formHeading.setText(item == null ? "" : StringsB.UC4_FORM_HEADING.formatted(item.maidName()));
        if (item != null && item.reviewed()) {
            formHeading.setText(StringsB.UC4_ERR_DUPLICATE);
        }
    }

    private void submit() {
        PastHire item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            AlertHelper.showError(StringsB.UC4_ERR_SELECT);
            return;
        }
        try {
            controller.submitReview(item.hire().getHireId(), overall.getRating(), punctuality.getRating(),
                    honesty.getRating(), cleanliness.getRating(), behaviour.getRating(),
                    rehire.isSelected(), comment.getText());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            return;
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
            return;
        }
        AlertHelper.showInfo(StringsB.UC4_THANKS);
        resetForm();
        load();
    }

    private void resetForm() {
        for (StarRating stars : new StarRating[] {overall, punctuality, honesty, cleanliness, behaviour}) {
            stars.setRating(0);
        }
        rehire.setSelected(false);
        comment.clear();
        table.getSelectionModel().clearSelection();
    }

    private static TableColumn<PastHire, String> column(String title, double width, Function<PastHire, String> value) {
        TableColumn<PastHire, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}