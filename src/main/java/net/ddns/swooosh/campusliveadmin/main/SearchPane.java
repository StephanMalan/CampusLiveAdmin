package net.ddns.swooosh.campusliveadmin.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.admin.AdminSearch;

public class SearchPane extends VBox {

    private ObservableList<AdminSearch> searches = FXCollections.observableArrayList();
    private FilteredList<AdminSearch> filteredList = new FilteredList<>(searches, p -> true);

    public SearchPane() {
        init();
    }

    private void init() {
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
    }

}
