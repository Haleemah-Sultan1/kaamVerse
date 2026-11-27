package kaamverse.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.HiringController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.AvailabilitySlot;
import kaamverse.bl.model.HireService;
import kaamverse.bl.model.Review;
import kaamverse.bl.service.HiringService.HireSummary;
import kaamverse.bl.service.HiringService.MaidProfile;
import kaamverse.bl.service.HiringService.ServiceOffer;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;

// UC3: one maid's profile, and the hire request form with summary and confirm.
public class MaidProfileScreen extends BorderPane {

    private final SceneNavigator navigator;
    private final HiringController controller = new HiringController();
    private final int maidId;
    private final Runnable onBack;
    private final Map<Integer, CheckBox> checks = new LinkedHashMap<>();
    private final Map<Integer, TextField> quantities = new LinkedHashMap<>();
    private final TextField hoursField = new TextField();

    public MaidProfileScreen(SceneNavigator navigator, int maidId, Runnable onBack) {
        this.navigator = navigator;
        this.maidId = maidId;
        this.onBack = onBack;
        setPadding(new Insets(16, 24, 16, 24));

        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> onBack.run());
        Button send = new Button(StringsB.UC3_SEND);
        send.setOnAction(event -> sendRequest());
        HBox buttons = new HBox(10, send, back);
        setBottom(buttons);

        try {
            setCenter(buildContent(controller.openMaidProfile(maidId)));
        } catch (ValidationException e) {
            setCenter(new Label(e.getMessage()));
            send.setDisable(true);
        } catch (DatabaseException e) {
            setCenter(new Label(Strings.DATABASE_ERROR));
            send.setDisable(true);
        }
    }

    private ScrollPane buildContent(MaidProfile profile) {
        Label heading = new Label(StringsB.UC3_PROFILE_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        String badge = profile.maid().hasBadge() ? "   [" + StringsB.UC3_BADGE + "]" : "";
        Label name = new Label(profile.maid().getName() + badge);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        double rating = profile.maid().getAverageRating();
        Label info = new Label(StringsB.UC3_PROFILE_RATING.formatted(
                rating <= 0 ? StringsB.UC3_NO_RATING : String.format("%.1f / 5", rating))
                + "     " + StringsB.UC3_PROFILE_AREA.formatted(profile.area()));
        Label slots = new Label(slotsText(profile.slots()));
        slots.setWrapText(true);

        Label hireHeading = new Label(StringsB.UC3_HIRE_HEADING);
        hireHeading.setStyle("-fx-font-weight: bold;");
        Label reviewHeading = new Label(StringsB.UC3_PROFILE_REVIEWS);
        reviewHeading.setStyle("-fx-font-weight: bold;");

        VBox content = new VBox(10, heading, name, info, slots, hireHeading, buildOfferGrid(profile),
                new Label(StringsB.UC3_HOURS), hoursField, reviewHeading, buildReviews(profile));
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        BorderPane.setMargin(scroll, new Insets(12, 0, 12, 0));
        return scroll;
    }

    private GridPane buildOfferGrid(MaidProfile profile) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.addRow(0, new Label(""), new Label(StringsB.UC3_COL_OFFER), new Label(StringsB.UC3_COL_EXPERIENCE),
                new Label(StringsB.UC3_COL_WAGE), new Label(StringsB.UC3_COL_QUANTITY));
        int row = 1;
        for (ServiceOffer offer : profile.offers()) {
            int serviceId = offer.service().getServiceId();
            CheckBox check = new CheckBox();
            TextField quantity = new TextField();
            quantity.setPrefWidth(110);
            quantity.disableProperty().bind(check.selectedProperty().not());
            String wage = StringsB.UC3_NO_BAND;
            if (offer.band() == null) {
                check.setDisable(true);
            } else {
                wage = HireSearchScreen.bandText(offer);
                quantity.setPromptText(StringsB.quantityName(offer.band().getUnit()));
            }
            checks.put(serviceId, check);
            quantities.put(serviceId, quantity);
            grid.addRow(row, check, new Label(offer.service().getName()),
                    new Label(StringsB.UC3_PROFILE_YEARS.formatted(offer.years())), new Label(wage), quantity);
            row++;
        }
        return grid;
    }

    private ListView<String> buildReviews(MaidProfile profile) {
        ListView<String> list = new ListView<>();
        list.setPrefHeight(150);
        list.setPlaceholder(new Label(StringsB.UC3_PROFILE_NO_REVIEWS));
        for (Review review : profile.reviews()) {
            String rehire = review.isWouldRehire() ? StringsB.UC3_WOULD_REHIRE : StringsB.UC3_WOULD_NOT_REHIRE;
            String comment = review.getComment() == null ? "" : review.getComment();
            list.getItems().add(StringsB.UC3_REVIEW_LINE.formatted(review.getOverallRating(), rehire, comment));
        }
        return list;
    }

    private void sendRequest() {
        Map<Integer, String> chosen = new LinkedHashMap<>();
        for (Map.Entry<Integer, CheckBox> entry : checks.entrySet()) {
            if (entry.getValue().isSelected()) {
                chosen.put(entry.getKey(), quantities.get(entry.getKey()).getText());
            }
        }
        HireSummary summary;
        try {
            summary = controller.sendHireRequest(maidId, chosen, hoursField.getText());
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            return;
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
            return;
        }
        if (!AlertHelper.confirm(summaryText(summary))) {
            return;
        }
        try {
            controller.confirmHireRequest();
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
            return;
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
            return;
        }
        AlertHelper.showInfo(StringsB.UC3_SAVED);
        navigator.show(new MyHiresScreen(navigator), Strings.APP_TITLE);
    }

    private String summaryText(HireSummary summary) {
        StringBuilder text = new StringBuilder(StringsB.UC3_SUMMARY_HEAD.formatted(summary.maidName()));
        text.append("\n\n");
        for (HireService line : summary.hire().getServices()) {
            text.append(StringsB.UC3_SUMMARY_LINE.formatted(line.getService().getName(),
                    String.format("%.2f", line.getQuantity()), StringsB.quantityName(line.getUnit()),
                    String.format("%,.2f", line.getWage()))).append('\n');
        }
        text.append('\n').append(StringsB.UC3_SUMMARY_HOURS.formatted(summary.hire().getWorkingHours()));
        text.append('\n').append(StringsB.UC3_SUMMARY_TOTAL.formatted(
                String.format("%,.2f", summary.hire().getCalculatedWage())));
        text.append('\n').append(StringsB.UC3_SUMMARY_RULE);
        text.append("\n\n").append(StringsB.UC3_SUMMARY_ASK);
        return text.toString();
    }

    private static String slotsText(List<AvailabilitySlot> slots) {
        if (slots.isEmpty()) {
            return StringsB.UC3_PROFILE_NO_SLOTS;
        }
        List<String> parts = new ArrayList<>();
        for (AvailabilitySlot slot : slots) {
            parts.add(slot.getDay() + " " + slot.getTimeSlot().name().toLowerCase());
        }
        return StringsB.UC3_PROFILE_AVAILABLE.formatted(String.join(", ", parts));
    }
}