package net.ddns.swooosh.campusliveadmin.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.all.Notification;

public class AddEditNotificationDialog extends CustomDialogSkin {

    public AddEditNotificationDialog(Window parent, ConnectionHandler connectionHandler, Notification notification) {
        initOwner(parent);
        String heading;
        if (notification != null) {
            heading = "Edit Notification";
        } else {
            heading = "Add Notification";
        }
        Text headingText = new Text(heading);
        TextField headingTextField = new TextField();
        headingTextField.setPromptText("Heading");
        headingTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField descriptionTextField = new TextField();
        descriptionTextField.setPromptText("Description");
        descriptionTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField studentNumberTextField = new TextField();
        studentNumberTextField.setPromptText("Student Number");
        studentNumberTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (!headingTextField.getText().isEmpty()) {
                if (!descriptionTextField.getText().isEmpty()) {
                    if (!studentNumberTextField.getText().isEmpty() && studentNumberTextField.getText().matches("[A-Z]{2}20[0-9]{2}-[0-9]{4}")) {
                        if (notification != null) {
                            if (!notification.getHeading().equals(headingTextField.getText()) || !notification.getDescription().equals(descriptionTextField.getText()) || !notification.getTag().equals(studentNumberTextField.getText())) {
                                connectionHandler.sendNotification(new Notification(notification.getId(), headingTextField.getText(), descriptionTextField.getText(), studentNumberTextField.getText()));
                            }
                        } else {
                            connectionHandler.sendNotification(new Notification(-1, headingTextField.getText(), descriptionTextField.getText(), studentNumberTextField.getText()));
                        }
                        closeAnimation();
                    } else {
                        UserNotification.showErrorMessage(heading, "Invalid Student Number");
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
        if (notification != null) {
            actionButton.setText("Edit");
            headingText.setText(notification.getHeading());
            descriptionTextField.setText(notification.getDescription());
            studentNumberTextField.setText(notification.getTag());
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, headingTextField, descriptionTextField, studentNumberTextField, buttonPane);
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
