package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.admin.AdminSearch;

public class SearchPane extends VBox {

    ObservableList<AdminSearch> searches = FXCollections.observableArrayList();
    private FilteredList<AdminSearch> filteredList = new FilteredList<>(searches, p -> true);
    ListView<AdminSearch> searchListView;
    Button addNewButton;

    public SearchPane() {
        init();
    }

    private void init() {
        setMinWidth(250);
        setMaxWidth(250);
        TextField searchTextField = new TextField();
        searchTextField.setPromptText("Search");
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate((AdminSearch adminSearch) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                } else if (adminSearch.getPrimaryText().toLowerCase().contains(newValue.toLowerCase()) || adminSearch.getSecondaryText().toLowerCase().contains(newValue.toLowerCase())) {
                    return true;
                }
                return false;
            });
        });
        searchListView = new ListView<>(filteredList);
        final AdminSearch[] prevValue = new AdminSearch[1];
        searchListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                prevValue[0] = newValue;
            }
        });
        searchListView.getItems().addListener((InvalidationListener) e -> {
            Platform.runLater(() -> {
                if (!searchListView.getItems().isEmpty()) {
                    for (int i = 0; i < searchListView.getItems().size(); i++) {
                        if (prevValue[0] != null && searchListView.getItems().get(i).getSecondaryText().equals(prevValue[0].getSecondaryText())) {
                            searchListView.getSelectionModel().select(i);
                            searchListView.getFocusModel().focus(i);
                        }
                    }

                }
            });
        });
        addNewButton = new Button("Add new");
        VBox.setVgrow(searchListView, Priority.ALWAYS);
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER);
        getChildren().addAll(searchTextField, searchListView, addNewButton);
    }

}
