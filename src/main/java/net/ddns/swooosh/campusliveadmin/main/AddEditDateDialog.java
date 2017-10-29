package net.ddns.swooosh.campusliveadmin.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.all.ImportantDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AddEditDateDialog extends CustomDialogSkin {

    public AddEditDateDialog(Window parent, ConnectionHandler connectionHandler, ImportantDate importantDate) {
        initOwner(parent);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String heading;
        if (importantDate != null) {
            heading = "Edit Import Date";
        } else {
            heading = "Add Important Date";
        }
        Text headingText = new Text(heading);
        TextField descriptionTextField = new TextField();
        descriptionTextField.setPromptText("Description");
        descriptionTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select date");
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (!descriptionTextField.getText().isEmpty()) {
                if (datePicker.getValue() != null) {
                    if (importantDate != null) {
                        if (!importantDate.getDescription().equals(descriptionTextField.getText()) || !importantDate.getDate().equals(datePicker.getValue().format(dateTimeFormatter))) {
                            connectionHandler.sendDate(new ImportantDate(importantDate.getId(), datePicker.getValue().format(dateTimeFormatter), descriptionTextField.getText()));
                        }
                    } else {
                        connectionHandler.sendDate(new ImportantDate(-1,  datePicker.getValue().format(dateTimeFormatter), descriptionTextField.getText()));
                    }
                    closeAnimation();
                } else {
                    UserNotification.showErrorMessage(heading, "Select valid date");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid Description");
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (importantDate != null) {
            actionButton.setText("Edit");
            descriptionTextField.setText(importantDate.getDescription());
            try {
                datePicker.setValue(dateFormat.parse(importantDate.getDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, descriptionTextField, datePicker, buttonPane);
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
