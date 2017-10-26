package net.ddns.swooosh.campusliveadmin.main;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.all.Lecturer;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddEditLecturerDialog extends CustomDialogSkin{

    public AddEditLecturerDialog(Window parent, ConnectionHandler connectionHandler, Lecturer lecturer) {
        initOwner(parent);
        String heading;
        if (lecturer != null) {
            heading = "Edit Lecturer";
        } else {
            heading = "Add Lecturer";
        }
        Text headingText = new Text(heading);
        TextField lecturerNumberTextField = new TextField();
        lecturerNumberTextField.setPromptText("Lecturer Number");
        lecturerNumberTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField firstNameTextField = new TextField();
        firstNameTextField.setPromptText("First Name");
        firstNameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField lastNameTextField = new TextField();
        lastNameTextField.setPromptText("Last Name");
        lastNameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Email");
        emailTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        ContactNumberTextField contactNumberTextField = new ContactNumberTextField("Contact Number");
        List<byte[]> imageBytes = new ArrayList<>();
        imageBytes.set(0, null);
        Circle lecturerPicture = new Circle(30);
        lecturerPicture.setStroke(Color.BLACK);
        lecturerPicture.setStrokeWidth(2);
        lecturerPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
        Button changePictureButton = new Button("Change Picture");
        changePictureButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            fileChooser.getExtensionFilters().addAll(filterJPG);
            File file = fileChooser.showOpenDialog(null);
            try {
                imageBytes.set(0, Files.readAllBytes(file.toPath()));
                lecturerPicture.setFill(new ImagePattern(SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(imageBytes.get(0))), null)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        HBox changePicturePane = new HBox(lecturerPicture, changePictureButton);
        changePicturePane.setSpacing(15);
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (lecturerNumberTextField.getText().matches("[A-Z]{2}[0-9]{3}")) {
                if (!firstNameTextField.getText().isEmpty() && firstNameTextField.getText().matches("[a-zA-Z ]*")) {
                    if (!lastNameTextField.getText().isEmpty() && lastNameTextField.getText().matches("[a-zA-Z ]*")) {
                        if (!emailTextField.getText().isEmpty() && emailTextField.getText().matches("^[a-z0-9](\\.?[a-z0-9]){5,}@gmail\\.com$")) {
                            if (!contactNumberTextField.getText().isEmpty() && contactNumberTextField.getText().matches("[0-9 ]{12}")) {
                                if (lecturer != null) {
                                    if (!firstNameTextField.getText().equals(lecturer.getFirstName()) || !lastNameTextField.getText().equals(lecturer.getLastName()) || !emailTextField.getText().equals(lecturer.getEmail()) || contactNumberTextField.getText().equals(lecturer.getContactNumber()) || imagesDiffer(lecturer.getImageBytes(), imageBytes.get(0))) {
                                        connectionHandler.sendLecturer(new Lecturer(firstNameTextField.getText(), lastNameTextField.getText(), lecturerNumberTextField.getText(), emailTextField.getText(), contactNumberTextField.getText(), imageBytes.get(0), null));
                                    }
                                } else {
                                    connectionHandler.sendLecturer(new Lecturer(firstNameTextField.getText(), lastNameTextField.getText(), lecturerNumberTextField.getText(), emailTextField.getText(), contactNumberTextField.getText(), imageBytes.get(0), null));
                                }
                                closeAnimation();
                            } else {
                                UserNotification.showErrorMessage(heading, "Invalid email");
                            }
                        } else {
                            UserNotification.showErrorMessage(heading, "Invalid email");
                        }
                    } else {
                        UserNotification.showErrorMessage(heading, "Invalid last name");
                    }
                } else {
                    UserNotification.showErrorMessage(heading, "Invalid first name");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid lecturer number");
            }

        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (lecturer != null) {
            actionButton.setText("Edit");
            lecturerNumberTextField.setText(lecturer.getLecturerNumber());
            lecturerNumberTextField.setEditable(false);
            firstNameTextField.setText(lecturer.getFirstName());
            lastNameTextField.setText(lecturer.getLastName());
            emailTextField.setText(lecturer.getEmail());
            contactNumberTextField.setText(lecturer.getContactNumber());
            if (lecturer.getImage() != null) {
                lecturerPicture.setFill(new ImagePattern(lecturer.getImage()));
            }
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, lecturerNumberTextField, firstNameTextField, lastNameTextField, emailTextField, contactNumberTextField, changePicturePane, buttonPane);
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

    private Boolean imagesDiffer(byte[] original, byte[] newImage) {
        if (original == newImage) return true;
        if (original == null || newImage == null) return false;
        return Arrays.equals(original, newImage);
    }

}
