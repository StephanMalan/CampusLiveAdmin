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
import models.all.Attendance;

public class EditAttendanceDialog extends CustomDialogSkin {

    public EditAttendanceDialog(Window parent, ConnectionHandler connectionHandler, Attendance attendance) {
        initOwner(parent);
        Text headingText = new Text("Edit Attendance");
        TextField attendanceDateTextField = new TextField(attendance.getAttendanceDate());
        attendanceDateTextField.setEditable(false);
        String[] attendanceStrings = new String[]{"Present", "Absent", "Late", "Left Early"};
        ComboBox<String> attendanceComboBox = new ComboBox<>(FXCollections.observableArrayList(attendanceStrings));
        attendanceComboBox.setPromptText("Attendance");
        if (attendance.getAttendance().equals("P")) {
            attendanceComboBox.getSelectionModel().select("Present");
        } else if (attendance.getAttendance().equals("A")) {
            attendanceComboBox.getSelectionModel().select("Absent");
        } else if (attendance.getAttendance().equals("L")) {
            attendanceComboBox.getSelectionModel().select("Late");
        } else if (attendance.getAttendance().equals("E")) {
            attendanceComboBox.getSelectionModel().select("Left Early");
        }
        Button actionButton = new Button("Edit");
        actionButton.setOnAction(e -> {
            if (!attendanceComboBox.getSelectionModel().isEmpty()) {
                String attendanceString;
                if (attendanceComboBox.getSelectionModel().getSelectedItem().equals("Present")) {
                    attendanceString = "P";
                } else if (attendanceComboBox.getSelectionModel().getSelectedItem().equals("Absent")) {
                    attendanceString = "A";
                } else if (attendanceComboBox.getSelectionModel().getSelectedItem().equals("Late")) {
                    attendanceString = "L";
                } else {
                    attendanceString = "E";
                }
                if (!attendance.getAttendance().equals(attendanceString)) {
                    attendance.setAttendance(attendanceString);
                    connectionHandler.sendAttendance(attendance);
                }
            } else {
                UserNotification.showErrorMessage("Edit Attendance", "Please select attendance");
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        VBox innerPane = new VBox(headingText, attendanceDateTextField, attendanceComboBox, buttonPane);
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
