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
import models.all.Student;

public class AddEditStudentDialog extends CustomDialogSkin {

    public AddEditStudentDialog(Window parent, ConnectionHandler connectionHandler, Student student) {
        initOwner(parent);
        String heading;
        if (student != null) {
            heading = "Edit Student";
        } else {
            heading = "Add Student";
        }
        Text headingText = new Text(heading);
        TextField studentNumberTextField = new TextField();
        studentNumberTextField.setPromptText("Student Number");
        studentNumberTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField firstNameTextField = new TextField();
        firstNameTextField.setPromptText("First Name");
        firstNameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField lastNameTextField = new TextField();
        lastNameTextField.setPromptText("Last Name");
        lastNameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        ComboBox<String> qualificationComboBox = new ComboBox<>(FXCollections.observableArrayList("BSc IT", "BCom"));
        qualificationComboBox.setPromptText("Qualification");
        qualificationComboBox.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Email");
        emailTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        ContactNumberTextField contactNumberTextField = new ContactNumberTextField("Contact Number");
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (studentNumberTextField.getText().length() >= 5 && studentNumberTextField.getText().matches("[A-Z]{2}[0-9]{4}-[0-9]{4}")) {
                if (!firstNameTextField.getText().isEmpty() && firstNameTextField.getText().matches("[a-zA-Z ]*")) {
                    if (!lastNameTextField.getText().isEmpty() && lastNameTextField.getText().matches("[a-zA-Z ]*")) {
                        if (!qualificationComboBox.getSelectionModel().isEmpty()) {
                            if (!emailTextField.getText().isEmpty() && emailTextField.getText().matches("^[a-z0-9](\\.?[a-z0-9]){5,}@gmail\\.com$")) {
                                if (!contactNumberTextField.getText().isEmpty() && contactNumberTextField.getText().matches("[0-9 ]{12}")) {
                                    if (student != null) {
                                        if (!student.getStudentNumber().equals(studentNumberTextField.getText()) || !student.getFirstName().equals(firstNameTextField.getText()) || !student.getLastName().equals(lastNameTextField.getText()) || !student.getQualification().equals(qualificationComboBox.getSelectionModel().getSelectedItem()) || !student.getEmail().equals(emailTextField.getText()) || !student.getContactNumber().equals(contactNumberTextField.getText())) {
                                            connectionHandler.sendStudent(new Student(studentNumberTextField.getText(), qualificationComboBox.getSelectionModel().getSelectedItem(), firstNameTextField.getText(), lastNameTextField.getText(), emailTextField.getText(), contactNumberTextField.getText(), null));
                                        }
                                    } else {
                                        connectionHandler.sendStudent(new Student(studentNumberTextField.getText(), qualificationComboBox.getSelectionModel().getSelectedItem(), firstNameTextField.getText(), lastNameTextField.getText(), emailTextField.getText(), contactNumberTextField.getText(), null));
                                    }
                                    closeAnimation();
                                } else {
                                    UserNotification.showErrorMessage(heading, "Invalid Contact Number");
                                }
                            } else {
                                UserNotification.showErrorMessage(heading, "Invalid Last Name");
                            }
                        } else {
                            UserNotification.showErrorMessage(heading, "Select qualification");
                        }
                    } else {
                        UserNotification.showErrorMessage(heading, "Invalid Last Name");
                    }
                } else {
                    UserNotification.showErrorMessage(heading, "Invalid First Name");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid Student Number");
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (student != null) {
            actionButton.setText("Edit");
            studentNumberTextField.setText(student.getStudentNumber());
            studentNumberTextField.setEditable(false);
            firstNameTextField.setText(student.getFirstName());
            lastNameTextField.setText(student.getLastName());
            qualificationComboBox.getSelectionModel().select(student.getQualification());
            emailTextField.setText(student.getEmail());
            contactNumberTextField.setText(student.getEmail());
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, studentNumberTextField, firstNameTextField, lastNameTextField, qualificationComboBox, emailTextField, contactNumberTextField, buttonPane);
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
