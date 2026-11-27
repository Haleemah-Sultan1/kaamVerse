package kaamverse.ui;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import kaamverse.bl.controller.FamilyMemberForm;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.controller.WelfareController;
import kaamverse.bl.model.EmergencyContact;
import kaamverse.bl.model.FamilyMember;
import kaamverse.bl.model.Maid;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;

/**
 * UC8: search a maid, then add, edit and delete her family members and
 * emergency contacts. Talks to WelfareController only (GRASP Low Coupling).
 */
public class FamilyRecordsScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final WelfareController controller = new WelfareController();

    private final TextField searchField = new TextField();
    private final ComboBox<Maid> maidChoice = new ComboBox<>();
    private final TableView<FamilyMember> familyTable = new TableView<>();
    private final TableView<EmergencyContact> contactTable = new TableView<>();
    private final VBox records = new VBox(12);

    public FamilyRecordsScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(Strings.UC8_FAMILY);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        searchField.setPromptText(Strings.UC8_SEARCH_PROMPT);
        searchField.setPrefWidth(240);
        searchField.setOnAction(event -> search());
        Button search = new Button(Strings.UC8_SEARCH);
        search.setDefaultButton(true);
        search.setOnAction(event -> search());
        maidChoice.setPrefWidth(300);
        maidChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Maid maid) {
                return maid == null ? "" : maid.getName() + " (" + maid.getCnic() + ")";
            }

            @Override
            public Maid fromString(String text) {
                return null;
            }
        });
        maidChoice.valueProperty().addListener((observable, old, chosen) -> loadRecords());
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        HBox searchRow = new HBox(10, searchField, search, maidChoice, back);

        buildFamilyTable();
        buildContactTable();
        records.getChildren().addAll(familySection(), contactSection());
        records.setVisible(false);

        VBox top = new VBox(12, heading, searchRow);
        setTop(top);
        setCenter(records);
        BorderPane.setMargin(records, new Insets(12, 0, 0, 0));
    }

    // Search

    private void search() {
        List<Maid>[] found = newHolder();
        if (!attempt(() -> found[0] = controller.searchMaids(searchField.getText()))) {
            return;
        }
        maidChoice.getItems().setAll(found[0]);
        records.setVisible(false);
        if (found[0].isEmpty()) {
            AlertHelper.showInfo(Strings.UC8_NO_MAIDS);
        } else {
            maidChoice.getSelectionModel().selectFirst();
            if (found[0].size() == 1) {
                loadRecords(); // the listener does not fire if she was already selected
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T>[] newHolder() {
        return new List[1];
    }

    private void loadRecords() {
        Maid maid = maidChoice.getValue();
        if (maid == null) {
            records.setVisible(false);
            return;
        }
        boolean loaded = attempt(() -> {
            familyTable.getItems().setAll(controller.getFamilyMembers(maid.getMaidId()));
            contactTable.getItems().setAll(controller.getEmergencyContacts(maid.getMaidId()));
        });
        records.setVisible(loaded);
    }

    // Family members

    private void buildFamilyTable() {
        familyTable.getColumns().add(column(Strings.UC8_COL_NAME, 150, FamilyMember::getName));
        familyTable.getColumns().add(column(Strings.UC8_COL_RELATION, 100, FamilyMember::getRelation));
        familyTable.getColumns().add(column(Strings.UC8_COL_AGE, 50, member -> String.valueOf(member.getAge())));
        familyTable.getColumns().add(column(Strings.UC8_COL_CNIC, 130, member -> nullToEmpty(member.getCnicOrBForm())));
        familyTable.getColumns().add(column(Strings.UC8_COL_DEPENDENT, 80,
                member -> member.isDependent() ? Strings.UC10_YES : Strings.UC10_NO));
        familyTable.getColumns().add(column(Strings.UC8_COL_SCHOOL, 120, member -> nullToEmpty(member.getSchool())));
        familyTable.getColumns().add(column(Strings.UC8_COL_PHONE, 110, member -> nullToEmpty(member.getContactNumber())));
        familyTable.setPrefHeight(180);
    }

    private VBox familySection() {
        Button add = new Button(Strings.UC8_ADD);
        add.setOnAction(event -> showFamilyDialog(null));
        Button edit = new Button(Strings.UC8_EDIT);
        edit.disableProperty().bind(familyTable.getSelectionModel().selectedItemProperty().isNull());
        edit.setOnAction(event -> showFamilyDialog(familyTable.getSelectionModel().getSelectedItem()));
        Button delete = new Button(Strings.UC8_DELETE);
        delete.disableProperty().bind(familyTable.getSelectionModel().selectedItemProperty().isNull());
        delete.setOnAction(event -> deleteFamilyMember(familyTable.getSelectionModel().getSelectedItem()));
        return section(Strings.UC8_FAMILY_HEADING, familyTable, add, edit, delete);
    }

    private void deleteFamilyMember(FamilyMember member) {
        if (member != null && AlertHelper.confirm(Strings.UC8_CONFIRM_DELETE_FAMILY.formatted(member.getName()))) {
            attempt(() -> controller.deleteFamilyMember(maidChoice.getValue().getMaidId(), member));
            loadRecords();
        }
    }

    /** Add (existing == null) or edit form. Errors keep the dialog open. */
    private void showFamilyDialog(FamilyMember existing) {
        TextField name = new TextField(existing == null ? "" : existing.getName());
        TextField relation = new TextField(existing == null ? "" : existing.getRelation());
        TextField age = new TextField(existing == null ? "" : String.valueOf(existing.getAge()));
        TextField idNumber = new TextField(existing == null ? "" : nullToEmpty(existing.getCnicOrBForm()));
        CheckBox dependent = new CheckBox(Strings.UC8_FIELD_DEPENDENT);
        dependent.setSelected(existing != null && existing.isDependent());
        TextField school = new TextField(existing == null ? "" : nullToEmpty(existing.getSchool()));
        TextField phone = new TextField(existing == null ? "" : nullToEmpty(existing.getContactNumber()));

        GridPane form = formGrid();
        form.addRow(0, new Label(Strings.UC8_COL_NAME), name);
        form.addRow(1, new Label(Strings.UC8_COL_RELATION), relation);
        form.addRow(2, new Label(Strings.UC8_COL_AGE), age);
        form.addRow(3, new Label(Strings.UC8_COL_CNIC), idNumber);
        form.add(dependent, 1, 4);
        form.addRow(5, new Label(Strings.UC8_COL_SCHOOL), school);
        form.addRow(6, new Label(Strings.UC8_COL_PHONE), phone);

        int maidId = maidChoice.getValue().getMaidId();
        showForm(existing == null ? Strings.UC8_ADD_FAMILY_TITLE : Strings.UC8_EDIT_FAMILY_TITLE, form, () -> {
            FamilyMemberForm values = new FamilyMemberForm(name.getText(), relation.getText(), age.getText(),
                    idNumber.getText(), dependent.isSelected(), school.getText(), phone.getText());
            if (existing == null) {
                controller.addFamilyMember(maidId, values);
            } else {
                controller.editFamilyMember(maidId, existing.getFamilyMemberId(), values);
            }
        });
    }

    // Emergency contacts

    private void buildContactTable() {
        contactTable.getColumns().add(column(Strings.UC8_COL_NAME, 200, EmergencyContact::getName));
        contactTable.getColumns().add(column(Strings.UC8_COL_RELATION, 150, EmergencyContact::getRelation));
        contactTable.getColumns().add(column(Strings.UC8_COL_PHONE, 150, EmergencyContact::getPhoneNumber));
        contactTable.setPrefHeight(140);
    }

    private VBox contactSection() {
        Button add = new Button(Strings.UC8_ADD);
        add.setOnAction(event -> showContactDialog());
        Button delete = new Button(Strings.UC8_DELETE);
        delete.disableProperty().bind(contactTable.getSelectionModel().selectedItemProperty().isNull());
        delete.setOnAction(event -> deleteContact(contactTable.getSelectionModel().getSelectedItem()));
        return section(Strings.UC8_CONTACT_HEADING, contactTable, add, delete);
    }

    private void deleteContact(EmergencyContact contact) {
        if (contact != null && AlertHelper.confirm(Strings.UC8_CONFIRM_DELETE_CONTACT.formatted(contact.getName()))) {
            attempt(() -> controller.deleteEmergencyContact(maidChoice.getValue().getMaidId(), contact));
            loadRecords();
        }
    }

    private void showContactDialog() {
        TextField name = new TextField();
        TextField relation = new TextField();
        TextField phone = new TextField();
        GridPane form = formGrid();
        form.addRow(0, new Label(Strings.UC8_COL_NAME), name);
        form.addRow(1, new Label(Strings.UC8_COL_RELATION), relation);
        form.addRow(2, new Label(Strings.UC8_COL_PHONE), phone);
        int maidId = maidChoice.getValue().getMaidId();
        showForm(Strings.UC8_ADD_CONTACT_TITLE, form,
                () -> controller.addEmergencyContact(maidId, name.getText(), relation.getText(), phone.getText()));
    }

    // Shared helpers

    /**
     * A Save / Cancel dialog. Save runs the action; if it fails the alert is
     * shown and the dialog stays open so nothing typed is lost.
     */
    private void showForm(String title, GridPane form, Runnable save) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(form);
        ButtonType saveType = new ButtonType(Strings.UC8_SAVE, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType,
                new ButtonType(Strings.UC8_CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE));
        Consumer<ActionEvent> onSave = event -> {
            if (!attempt(save)) {
                event.consume();
            }
        };
        dialog.getDialogPane().lookupButton(saveType).addEventFilter(ActionEvent.ACTION, onSave::accept);
        if (dialog.showAndWait().filter(button -> button == saveType).isPresent()) {
            loadRecords();
        }
    }

    private static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        return grid;
    }

    private static VBox section(String title, TableView<?> table, Button... buttons) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");
        return new VBox(6, label, table, new HBox(10, buttons));
    }

    private static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    /** Runs an action and turns each kind of failure into a friendly alert. */
    private boolean attempt(Runnable action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            if (e.getMessage().equals(Strings.UC8_ERR_RECORD_GONE)) {
                loadRecords();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        } catch (RuntimeException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UNEXPECTED_ERROR);
        }
        return false;
    }
}
