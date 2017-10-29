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
import models.all.Notice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AddEditNoticeDialog extends CustomDialogSkin {

    public AddEditNoticeDialog(Window parent, ConnectionHandler connectionHandler, Notice notice) {
        initOwner(parent);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String heading;
        if (notice != null) {
            heading = "Edit Notice";
        } else {
            heading = "Add Notice";
        }
        Text headingText = new Text(heading);
        TextField headingTextField = new TextField();
        headingTextField.setPromptText("Heading");
        headingTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField descriptionTextField = new TextField();
        descriptionTextField.setPromptText("Description");
        descriptionTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField tagTextField = new TextField();
        tagTextField.setPromptText("Last Name");
        tagTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        DatePicker expiryDatePicker = new DatePicker();
        expiryDatePicker.setPromptText("Select expiry date");
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (!headingTextField.getText().isEmpty()) {
                if (!descriptionTextField.getText().isEmpty()) {
                    if (!tagTextField.getText().isEmpty()) {
                        if (expiryDatePicker.getValue() != null && expiryDatePicker.getValue().isAfter(LocalDate.now())) {
                            if (notice != null) {
                                if (!notice.getHeading().equals(headingTextField.getText()) || !notice.getDescription().equals(descriptionTextField.getText()) || !notice.getTag().equals(tagTextField.getText()) || !notice.getExpiryDate().equals(expiryDatePicker.getValue().format(dateTimeFormatter))) {
                                    connectionHandler.sendNotice(new Notice(notice.getId(), headingTextField.getText(), descriptionTextField.getText(), tagTextField.getText(), expiryDatePicker.getValue().format(dateTimeFormatter)));
                                }
                            } else {
                                connectionHandler.sendNotice(new Notice(-1, headingTextField.getText(), descriptionTextField.getText(), tagTextField.getText(), expiryDatePicker.getValue().format(dateTimeFormatter)));
                            }
                            closeAnimation();
                        } else {
                            UserNotification.showErrorMessage(heading, "Select valid expiry date");
                        }
                    } else {
                        UserNotification.showErrorMessage(heading, "Invalid Tag");
                    }
                } else {
                    UserNotification.showErrorMessage(heading, "Invalid Description");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid Heading");
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (notice != null) {
            actionButton.setText("Edit");
            headingTextField.setText(notice.getHeading());
            descriptionTextField.setText(notice.getDescription());
            tagTextField.setText(notice.getTag());
            try {
                expiryDatePicker.setValue(dateFormat.parse(notice.getExpiryDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, headingTextField, descriptionTextField, tagTextField, expiryDatePicker, buttonPane);
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
