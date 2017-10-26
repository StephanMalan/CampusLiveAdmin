package net.ddns.swooosh.campusliveadmin.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.admin.Admin;

public class AddEditAdminDialog extends CustomDialogSkin{

    public AddEditAdminDialog(Window parent, Admin admin, ConnectionHandler connectionHandler) {
        initOwner(parent);
        String heading;
        if (admin != null) {
            heading = "Edit Admin";
        } else {
            heading = "Add Admin";
        }
        Text headingText = new Text(heading);
        TextField usernameTextField = new TextField();
        usernameTextField.setPromptText("Username");
        usernameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Email");
        emailTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (usernameTextField.getText().length() >= 5 && usernameTextField.getText().matches("[a-zA-Z0-9]*")) {
                if (!emailTextField.getText().isEmpty() && emailTextField.getText().matches("^[a-z0-9](\\.?[a-z0-9]){5,}@gmail\\.com$")) {
                    Admin newAdmin = new Admin(usernameTextField.getText(), emailTextField.getText());
                    if (admin != null) {
                        if (!admin.getAdminName().equals(newAdmin.getAdminName()) || !admin.getAdminEmail().equals(newAdmin.getAdminEmail()))
                        connectionHandler.sendAdmin(newAdmin);
                    } else if (admin == null){
                        connectionHandler.sendAdmin(newAdmin);
                    }
                    closeAnimation();
                } else {
                    UserNotification.showErrorMessage(heading, "Invalid Email");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid Username");
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (admin != null) {
            actionButton.setText("Edit");
            usernameTextField.setText(admin.getAdminName());
            usernameTextField.setEditable(false);
            emailTextField.setText(admin.getAdminEmail());
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, usernameTextField, emailTextField, buttonPane);
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
