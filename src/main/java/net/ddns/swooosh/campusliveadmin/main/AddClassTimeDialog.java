package net.ddns.swooosh.campusliveadmin.main;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.all.ClassTime;

public class AddClassTimeDialog extends CustomDialogSkin {

    public AddClassTimeDialog(Window parent, ConnectionHandler connectionHandler, int classID) {
        initOwner(parent);
        Text headingText = new Text("Add Class Time");
        TextField roomNumberTextField = new TextField();
        roomNumberTextField.setPromptText("Room Number");
        roomNumberTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        ComboBox<String> dayOfWeekComboBox = new ComboBox<>(FXCollections.observableArrayList(weekdays));
        dayOfWeekComboBox.setPromptText("Day of Week");
        String[] timeSlots = {"08:00 - 08:45", "09:00 - 09:45", "10:00 - 10:45", "11:00 - 11:45", "12:00 - 12:45", "13:00 - 13:45", "14:00 - 14:45", "15:00 - 15:45", "16:00 - 16:45", "17:00 - 17:45", "18:00 - 18:45", "18:45 - 19:30", "19:30 - 20:15"};
        ComboBox<String> startSlotComboBox = new ComboBox<>(FXCollections.observableArrayList(timeSlots));
        startSlotComboBox.setPromptText("Start Time Slot");
        ComboBox<String> endSlotComboBox = new ComboBox<>(FXCollections.observableArrayList(timeSlots));
        endSlotComboBox.setPromptText("End Time Slot");
        HBox selectPane = new HBox(startSlotComboBox, endSlotComboBox);
        selectPane.setSpacing(15);
        selectPane.setAlignment(Pos.CENTER);
        Button actionButton = new Button("Edit");
        actionButton.setOnAction(e -> {
            if (!roomNumberTextField.getText().isEmpty()) {
                if (!dayOfWeekComboBox.getSelectionModel().isEmpty()) {
                    if (!startSlotComboBox.getSelectionModel().isEmpty()) {
                        if (!endSlotComboBox.getSelectionModel().isEmpty()) {
                            if (startSlotComboBox.getSelectionModel().getSelectedIndex() <= endSlotComboBox.getSelectionModel().getSelectedIndex()) {
                                connectionHandler.sendClassTime(new ClassTime(0, classID, roomNumberTextField.getText(), dayOfWeekComboBox.getSelectionModel().getSelectedIndex() + 1, startSlotComboBox.getSelectionModel().getSelectedIndex() + 1, endSlotComboBox.getSelectionModel().getSelectedIndex() + 1));
                                closeAnimation();
                            } else {
                                UserNotification.showErrorMessage("Add Class Time", "Start slot must be before end slot");
                            }
                        } else {
                            UserNotification.showErrorMessage("Add Class Time", "Please select end time slot");
                        }
                    } else {
                        UserNotification.showErrorMessage("Add Class Time", "Please select start time slot");
                    }
                    closeAnimation();
                } else {
                    UserNotification.showErrorMessage("Add Class Time", "Please select day of week");
                }
            } else {
                UserNotification.showErrorMessage("Add Class Time", "Invalid room number");
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        VBox innerPane = new VBox(headingText, roomNumberTextField, dayOfWeekComboBox, selectPane, buttonPane);
        innerPane.setPadding(new Insets(20, 50, 20, 50));
        innerPane.setSpacing(20);
        innerPane.setMinWidth(600);
        innerPane.setMaxWidth(600);
        innerPane.setAlignment(Pos.CENTER);
        innerPane.setStyle("-fx-background-color: #ffffff;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;");
        VBox contentPane = new VBox(innerPane);
        contentPane.setAlignment(Pos.CENTER);
        setWidth(600);
        getDialogPane().setContent(contentPane);
    }

}
