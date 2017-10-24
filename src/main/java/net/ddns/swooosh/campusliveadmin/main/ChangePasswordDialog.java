package net.ddns.swooosh.campusliveadmin.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class ChangePasswordDialog extends CustomDialogSkin {

    public ChangePasswordDialog(Window parent, ConnectionHandler connectionHandler) {
        initOwner(parent);
        VBox innerPane = new VBox();
        Text changePasswordHeading = new Text("Change Password");
        changePasswordHeading.getStyleClass().add("heading-text");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        newPasswordField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        Button changeButton = new Button("Change");
        changeButton.getStyleClass().add("dialog-button");
        changeButton.setOnAction(e -> {
            if (!newPasswordField.getText().isEmpty()) {
                if (newPasswordField.getText().length() >= 5) {
                    if (newPasswordField.getText().matches("[a-zA-Z0-9]*")) {
                        if (connectionHandler.changeDefaultPassword(newPasswordField.getText())) {
                            UserNotification.showConfirmationMessage("Change Password", "Successfully changed password");
                            closeAnimation();
                        } else {
                            UserNotification.showErrorMessage("Change Password", "Could not change password");
                        }
                    } else {
                        UserNotification.showErrorMessage("Change Password", "Only alphanumeric passwords are accepted (letters and numbers)");
                    }
                } else {
                    UserNotification.showErrorMessage("Change Password", "Password must be at least 5 characters long");
                }
            } else {
                UserNotification.showErrorMessage("Change Password", "Password can't be empty");
            }
        });
        HBox buttonPane = new HBox(changeButton);
        innerPane.getChildren().addAll(changePasswordHeading, newPasswordField, buttonPane);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(25);
        innerPane.setPadding(new Insets(20, 50, 20, 50));
        innerPane.setSpacing(20);
        innerPane.setMinWidth(600);
        innerPane.setMaxWidth(600);
        innerPane.setAlignment(Pos.CENTER);
        innerPane.setStyle("-fx-background-color: #007FA3;" +
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
