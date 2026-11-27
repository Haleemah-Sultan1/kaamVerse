package kaamverse.ui;

import java.time.LocalDate;
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import kaamverse.bl.controller.TrustBadgeController;
import kaamverse.bl.controller.ValidationException;
import kaamverse.bl.model.Hire;
import kaamverse.bl.model.Review;
import kaamverse.bl.service.TrustBadgeService.MaidStats;
import kaamverse.bl.service.TrustBadgeService.ReviewCase;
import kaamverse.common.DatabaseException;
import kaamverse.common.Strings;
import kaamverse.common.StringsB;

/**
 * UC5: the moderator checks flagged reviews, then awards badges to maids who
 * meet the rule. Talks to TrustBadgeController only (GRASP Low Coupling).
 */
public class TrustBadgeScreen extends BorderPane {

    private static final int COMMENT_PREVIEW = 60;

    private final SceneNavigator navigator;
    private final TrustBadgeController controller = new TrustBadgeController();
    private final TableView<ReviewCase> reviewTable = new TableView<>();
    private final TextArea details = new TextArea();
    private final TableView<MaidStats> badgeTable = new TableView<>();
    private final Button approveButton = new Button(StringsB.UC5_APPROVE);
    private final Button rejectButton = new Button(StringsB.UC5_REJECT);
    private final Button awardButton = new Button(StringsB.UC5_AWARD);

    public TrustBadgeScreen(SceneNavigator navigator) {
        this.navigator = navigator;
        setPadding(new Insets(16, 24, 16, 24));

        Label heading = new Label(StringsB.UC5_HEADING);
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        setTop(heading);

        Tab reviewsTab = new Tab(StringsB.UC5_TAB_REVIEWS, buildReviewsTab());
        Tab badgesTab = new Tab(StringsB.UC5_TAB_BADGES, buildBadgesTab());
        reviewsTab.setClosable(false);
        badgesTab.setClosable(false);
        TabPane tabs = new TabPane(reviewsTab, badgesTab);
        setCenter(tabs);
        BorderPane.setMargin(tabs, new Insets(12, 0, 12, 0));

        Button back = new Button(Strings.BACK);
        back.setOnAction(event -> navigator.goHome());
        setBottom(back);

        loadAll();
    }

    private VBox buildReviewsTab() {
        reviewTable.getColumns().add(column(StringsB.UC5_COL_MAID, 160, item -> item.maid().getName()));
        reviewTable.getColumns().add(column(StringsB.UC5_COL_EMPLOYER, 160, ReviewCase::employerName));
        reviewTable.getColumns().add(column(StringsB.UC5_COL_RATING, 80,
                item -> item.review().getOverallRating() + "/5"));
        reviewTable.getColumns().add(column(StringsB.UC5_COL_COMMENT, 300, item -> preview(item.review())));
        reviewTable.getColumns().add(column(StringsB.UC5_COL_DATE, 110,
                item -> String.valueOf(item.review().getCreatedDate())));
        reviewTable.setPlaceholder(new Label(StringsB.UC5_NO_REVIEWS));
        reviewTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, old, item) -> showDetails(item));

        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(9);

        approveButton.setOnAction(event -> decide(true));
        rejectButton.setOnAction(event -> decide(false));
        approveButton.disableProperty().bind(reviewTable.getSelectionModel().selectedItemProperty().isNull());
        rejectButton.disableProperty().bind(reviewTable.getSelectionModel().selectedItemProperty().isNull());

        VBox box = new VBox(10, reviewTable, details, new HBox(10, approveButton, rejectButton));
        box.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(reviewTable, Priority.ALWAYS);
        return box;
    }

    private VBox buildBadgesTab() {
        badgeTable.getColumns().add(column(StringsB.UC5_COL_MAID, 180, item -> item.maid().getName()));
        badgeTable.getColumns().add(column(StringsB.UC5_COL_COUNT, 130,
                item -> String.valueOf(item.approvedCount())));
        badgeTable.getColumns().add(column(StringsB.UC5_COL_AVERAGE, 90,
                item -> String.format("%.2f", item.average())));
        badgeTable.getColumns().add(column(StringsB.UC5_COL_POSITIVE, 130,
                item -> String.format("%.0f%%", item.positivePercentage())));
        badgeTable.getColumns().add(column(StringsB.UC5_COL_ELIGIBLE, 120,
                item -> item.eligible() ? StringsB.UC5_YES : StringsB.UC5_NO));
        badgeTable.getColumns().add(column(StringsB.UC5_COL_BADGE, 110,
                item -> item.hasBadge() ? StringsB.UC5_AWARDED : "-"));
        badgeTable.setPlaceholder(new Label(StringsB.UC5_NO_MAIDS));

        // enabled only for a maid who meets the rule and has no badge yet
        awardButton.setOnAction(event -> award());
        awardButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            MaidStats selected = badgeTable.getSelectionModel().getSelectedItem();
            return selected == null || !selected.eligible() || selected.hasBadge();
        }, badgeTable.getSelectionModel().selectedItemProperty()));

        Label rule = new Label(StringsB.UC5_RULE);
        rule.setWrapText(true);
        VBox box = new VBox(10, rule, badgeTable, awardButton);
        box.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(badgeTable, Priority.ALWAYS);
        return box;
    }

    private static <S> TableColumn<S, String> column(String title, double width, Function<S, String> value) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private static String preview(Review review) {
        String comment = review.getComment();
        if (comment == null || comment.isBlank()) {
            return StringsB.UC5_NO_COMMENT;
        }
        return comment.length() <= COMMENT_PREVIEW ? comment : comment.substring(0, COMMENT_PREVIEW) + "...";
    }

    private static String date(LocalDate date) {
        return date == null ? "-" : date.toString();
    }

    private void showDetails(ReviewCase item) {
        if (item == null) {
            details.clear();
            return;
        }
        Review review = item.review();
        Hire hire = item.hire();
        String comment = review.getComment() == null || review.getComment().isBlank()
                ? StringsB.UC5_NO_COMMENT : review.getComment();
        details.setText(StringsB.UC5_DETAILS.formatted(
                review.getOverallRating(), review.getPunctuality(), review.getHonesty(),
                review.getCleanliness(), review.getBehaviour(),
                review.isWouldRehire() ? StringsB.UC5_YES : StringsB.UC5_NO, comment,
                item.employerName(), hire.getWorkingHours(), date(hire.getStartDate()), date(hire.getEndDate())));
    }

    private void decide(boolean approve) {
        ReviewCase item = reviewTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            AlertHelper.showError(StringsB.UC5_ERR_SELECT);
            return;
        }
        String question = (approve ? StringsB.UC5_CONFIRM_APPROVE : StringsB.UC5_CONFIRM_REJECT)
                .formatted(item.maid().getName());
        if (!AlertHelper.confirm(question)) {
            return;
        }
        double[] average = new double[1];
        boolean done = attempt(() -> {
            if (approve) {
                average[0] = controller.approveReview(item.review().getReviewId());
            } else {
                controller.rejectReview(item.review().getReviewId());
            }
        });
        if (done) {
            AlertHelper.showInfo(approve
                    ? StringsB.UC5_APPROVED.formatted(item.maid().getName(), average[0])
                    : StringsB.UC5_REJECTED);
        }
        loadAll(); // reload even after a failure, so a stale row disappears
    }

    private void award() {
        MaidStats stats = badgeTable.getSelectionModel().getSelectedItem();
        if (stats == null) {
            AlertHelper.showError(StringsB.UC5_ERR_SELECT);
            return;
        }
        if (!AlertHelper.confirm(StringsB.UC5_CONFIRM_AWARD.formatted(stats.maid().getName()))) {
            return;
        }
        if (attempt(() -> controller.awardBadge(stats.maid().getMaidId()))) {
            AlertHelper.showInfo(StringsB.UC5_AWARDED_OK);
        }
        loadAll();
    }

    private void loadAll() {
        attempt(() -> {
            reviewTable.getItems().setAll(controller.getPendingReviews());
            badgeTable.getItems().setAll(controller.getMaidStats());
            details.clear();
        });
    }

    private boolean attempt(Runnable action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            AlertHelper.showError(e.getMessage());
        } catch (DatabaseException e) {
            AlertHelper.showDatabaseError();
        }
        return false;
    }
}