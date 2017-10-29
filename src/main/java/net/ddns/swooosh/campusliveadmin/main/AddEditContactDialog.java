package net.ddns.swooosh.campusliveadmin.main;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import models.all.ContactDetails;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddEditContactDialog extends CustomDialogSkin {

    public AddEditContactDialog(Window parent, ConnectionHandler connectionHandler, ContactDetails contactDetails) {
        initOwner(parent);
        String heading;
        if (contactDetails != null) {
            heading = "Edit Contact Details";
        } else {
            heading = "Add Contact Details";
        }
        Text headingText = new Text(heading);
        TextField nameTextField = new TextField();
        nameTextField.setPromptText("Name");
        nameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField positionTextField = new TextField();
        positionTextField.setPromptText("Position");
        positionTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        ComboBox<String> departmentComboBox = new ComboBox<>(FXCollections.observableArrayList("Campus", "BSc IT", "BCom"));
        departmentComboBox.setPromptText("Department");
        ContactNumberTextField contactNumberTextField = new ContactNumberTextField("Contact Number");
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Email");
        emailTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        List<byte[]> imageBytes = new ArrayList<>();
        imageBytes.add(0, null);
        Circle contactPicture = new Circle(30);
        contactPicture.setStroke(Color.BLACK);
        contactPicture.setStrokeWidth(2);
        contactPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
        Button changePictureButton = new Button("Change Picture");
        changePictureButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            fileChooser.getExtensionFilters().addAll(filterJPG);
            File file = fileChooser.showOpenDialog(null);
            try {
                if (file != null) {
                    imageBytes.set(0, Files.readAllBytes(file.toPath()));
                    contactPicture.setFill(new ImagePattern(SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(imageBytes.get(0))), null)));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        Button removePictureButton = new Button("Remove Picture");
        removePictureButton.setOnAction(e -> {
            imageBytes.set(0, null);
        });
        HBox changePicturePane = new HBox(contactPicture, changePictureButton, removePictureButton);
        changePicturePane.setSpacing(15);
        Button actionButton = new Button();
        actionButton.setOnAction(e -> {
            if (!nameTextField.getText().isEmpty()) {
                if (!positionTextField.getText().isEmpty()) {
                    if (!departmentComboBox.getSelectionModel().isEmpty()) {
                        if (!emailTextField.getText().isEmpty() && Display.validEmail(emailTextField.getText())) {
                            if (!contactNumberTextField.getNumber().isEmpty() && contactNumberTextField.getNumber().matches("[0-9 ]{12}")) {
                                if (contactDetails != null) {
                                    if (!nameTextField.getText().equals(contactDetails.getName()) || !positionTextField.getText().equals(contactDetails.getPosition()) || !departmentComboBox.getSelectionModel().getSelectedItem().equals(contactDetails.getDepartment()) || contactNumberTextField.getNumber().equals(contactDetails.getContactNumber()) || !emailTextField.getText().equals(contactDetails.getContactDetails()) || imagesDiffer(contactDetails.getImageBytes(), imageBytes.get(0))) {
                                        connectionHandler.sendContact(new ContactDetails(contactDetails.getId(), nameTextField.getText(), positionTextField.getText(), departmentComboBox.getSelectionModel().getSelectedItem(), contactNumberTextField.getNumber(), emailTextField.getText(), imageBytes.get(0)));
                                    }
                                } else {
                                    connectionHandler.sendContact(new ContactDetails(-1, nameTextField.getText(), positionTextField.getText(), departmentComboBox.getSelectionModel().getSelectedItem(), contactNumberTextField.getNumber(), emailTextField.getText(), imageBytes.get(0)));
                                }
                                closeAnimation();
                            } else {
                                UserNotification.showErrorMessage(heading, "Invalid contact number");
                            }
                        } else {
                            UserNotification.showErrorMessage(heading, "Invalid email");
                        }
                    } else {
                        UserNotification.showErrorMessage(heading, "Please select department");
                    }
                } else {
                    UserNotification.showErrorMessage(heading, "Invalid position");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid name");
            }

        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (contactDetails != null) {
            actionButton.setText("Edit");
            nameTextField.setText(contactDetails.getName());
            positionTextField.setText(contactDetails.getPosition());
            departmentComboBox.getSelectionModel().select(contactDetails.getDepartment());
            emailTextField.setText(contactDetails.getEmail());
            contactNumberTextField.setText(contactDetails.getContactNumber());
            if (contactDetails.getImage() != null) {
                contactPicture.setFill(new ImagePattern(contactDetails.getImage()));
            }
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox(headingText, nameTextField, positionTextField, departmentComboBox, emailTextField, contactNumberTextField, changePicturePane, buttonPane);
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
