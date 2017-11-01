package net.ddns.swooosh.campusliveadmin.main;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import models.all.ClassResultAttendance;
import models.all.StudentClass;

import java.util.List;

public class AddClassDialog extends CustomDialogSkin {

    public AddClassDialog(Window parent, ConnectionHandler connectionHandler, String studentNumber, String department, List<ClassResultAttendance> classResultAttendances) {
        initOwner(parent);
        Label classLabel = new Label("Select class to register to from dropdown");
        ObservableList<StudentClass> classes = connectionHandler.getAllClasses();
        for (ClassResultAttendance classResultAttendance : classResultAttendances) {
            for (StudentClass studentClass : classes) {
                if (studentClass.getClassID() == classResultAttendance.getStudentClass().getClassID()) {
                    classes.remove(studentClass);
                    break;
                }
            }
        }
        ComboBox<StudentClass> classComboBox = new ComboBox(classes);
        classComboBox.getSelectionModel().select(0);
        classComboBox.setCellFactory(e -> new ListCell<StudentClass>() {
            @Override
            protected void updateItem(StudentClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item.getClassID() == -1){
                    setText("No Classes");
                } else {
                    setText(item.getClassID() + ": " + item.getModuleNumber() + " - " + item.getClassLecturer().getLastName());
                }
            }
        });
        classComboBox.setButtonCell(new ListCell<StudentClass>() {
            @Override
            protected void updateItem(StudentClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getClassID() + ": " + item.getModuleNumber() + " - " + item.getClassLecturer().getLastName());
                }
            }
        });
        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            if (!classComboBox.getSelectionModel().isEmpty() && classComboBox.getSelectionModel().getSelectedItem().getClassID() != -1) {
                connectionHandler.registerClass(studentNumber, classComboBox.getSelectionModel().getSelectedItem().getClassID());
                closeAnimation();
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(registerButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(10);
        VBox contentPane = new VBox(classLabel, classComboBox, buttonPane);
        contentPane.setSpacing(15);
        contentPane.setPadding(new Insets(15));
        contentPane.setAlignment(Pos.CENTER);
        contentPane.setStyle("-fx-background-color: #D4EAE4");
        getDialogPane().setContent(contentPane);
    }

}
