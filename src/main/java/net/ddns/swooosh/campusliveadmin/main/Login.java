package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.all.UserNotification;

public class Login extends Application {

    private ConnectionHandler connectionHandler;
    Boolean loggedIn = false;

    public Login(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("CampusLive Admin Login");
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("CLLogo.png")));

        //Setup login pane
        TextField usernameTextField = new TextField("admin");
        usernameTextField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("admin");
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> {
            if (!usernameTextField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
                if (connectionHandler.login(usernameTextField.getText(), passwordField.getText())) {
                    loggedIn = true;
                    stage.close();
                } else {
                    UserNotification.showErrorMessage("Login failed", "Incorrect login details entered");
                }
            } else {
                UserNotification.showErrorMessage("Invalid inputs", "Invalid username or password entered");
            }
        });
        VBox loginPane = new VBox(usernameTextField, passwordField, loginButton);
        loginPane.setPadding(new Insets(25));
        loginPane.setSpacing(10);
        loginPane.setAlignment(Pos.CENTER);

        //Setup scene
        Scene scene = new Scene(loginPane, 300, 300);

        //Select and show scene
        stage.setScene(scene);
        stage.showAndWait();
    }
}
