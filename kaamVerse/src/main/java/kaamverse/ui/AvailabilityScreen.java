package kaamverse.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.AvailabilityController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.AvailabilitySlot;
import kaamverse.bl.model.SlotChoice;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

/**
 * UC7: a 7 x 3 grid of checkboxes for the maid's weekly availability.
 * Booked slots stay ticked and disabled. Talks to AvailabilityController
 * only (GRASP Low Coupling).
 */
public class AvailabilityScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final AvailabilityController controller = new AvailabilityController();
    private final Map<WeekDay, Map<TimeSlot, CheckBox>> boxes = new EnumMap<>(WeekDay.class);

    public AvailabilityScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(Strings.UC7_AVAILABILITY);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label hint = new Label(Strings.UC7_HINT);
        hint.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(32);
        grid.setVgap(12);
        grid.add(bold(Strings.UC7_DAY_SLOT), 0, 0);
        for (TimeSlot slot : TimeSlot.values()) {
            grid.add(bold(Strings.uc7TimeSlotName(slot)), slot.ordinal() + 1, 0);
        }
        for (WeekDay day : WeekDay.values()) {
            grid.add(new Label(Strings.uc7DayName(day)), 0, day.ordinal() + 1);
            Map<TimeSlot, CheckBox> row = new EnumMap<>(TimeSlot.class);
            for (TimeSlot slot : TimeSlot.values()) {
                CheckBox box = new CheckBox();
                row.put(slot, box);
                grid.add(box, slot.ordinal() + 1, day.ordinal() + 1);
                GridPane.setHalignment(box, javafx.geometry.HPos.CENTER);
            }
            boxes.put(day, row);
        }

        Button save = new Button(Strings.UC7_SAVE);
        save.setDefaultButton(true);
        save.setOnAction(event -> save());
        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        HBox buttons = new HBox(10, save, back);

        VBox body = new VBox(16, hint, grid, buttons);
        body.setPadding(new Insets(12, 0, 0, 0));
        body.setAlignment(Pos.TOP_LEFT);
        setTop(heading);
        setCenter(body);
        attempt(() -> show(controller.getAvailability()));
    }

    /** Ticks the saved slots; booked ones are also disabled. */
    private void show(List<AvailabilitySlot> saved) {
        boxes.values().forEach(row -> row.values().forEach(box -> {
            box.setSelected(false);
            box.setDisable(false);
            box.setTooltip(null);
        }));
        for (AvailabilitySlot slot : saved) {
            CheckBox box = boxes.get(slot.getDay()).get(slot.getTimeSlot());
            box.setSelected(true);
            if (slot.isBooked()) {
                box.setDisable(true);
                box.setTooltip(new Tooltip(Strings.UC7_BOOKED_TIP));
            }
        }
    }

    private void save() {
        List<SlotChoice> selected = new ArrayList<>();
        boxes.forEach((day, row) -> row.forEach((slot, box) -> {
            if (box.isSelected()) {
                selected.add(new SlotChoice(day, slot));
            }
        }));
        attempt(() -> {
            show(controller.saveAvailability(selected));
            AlertHelper.showInfo(Strings.UC7_SAVED);
        });
    }

    /** Runs an action and turns each kind of failure into a friendly alert. */
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            e.printStackTrace();
            AlertHelper.showDatabaseError();
        } catch (RuntimeException e) {
            e.printStackTrace();
            AlertHelper.showError(Strings.UNEXPECTED_ERROR);
        }
    }

    private static Label bold(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }
}
