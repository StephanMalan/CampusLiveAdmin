package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.admin.Admin;
import models.all.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;

public class Display extends Application {

    public static final File APPLICATION_FOLDER = new File(System.getProperty("user.home") + "/AppData/Local/Swooosh/CampusLiveAdmin");
    private ConnectionHandler connectionHandler = new ConnectionHandler();

    public void start(Stage stage) throws Exception {

        //<editor-fold desc="Login">
        Login login = new Login(connectionHandler);
        try {
            login.start(new Stage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!login.loggedIn) {
            System.exit(0);
        }
        //</editor-fold>

        //<editor-fold desc="Stage">
        //Setup stage
        stage.setMaximized(true);
        stage.setTitle("CampusLiveAdmin");
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("CLLogo.png")));
        //</editor-fold>

        //<editor-fold desc="Admin Pane">
        //Setup admin pane
        TableView<Admin> adminTableView = new TableView<>();
        adminTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        adminTableView.setMaxWidth(600);
        adminTableView.setMaxHeight(600);
        TableColumn<Admin, String> adminUsernameTableColumn = new TableColumn<>("Username");
        adminUsernameTableColumn.setCellValueFactory(new PropertyValueFactory<>("adminName"));
        adminUsernameTableColumn.setResizable(false);
        adminUsernameTableColumn.setMaxWidth(200);
        adminUsernameTableColumn.setMinWidth(200);
        TableColumn<Admin, String> adminEmailTableColumn = new TableColumn<>("Email");
        adminEmailTableColumn.setCellValueFactory(new PropertyValueFactory<>("adminEmail"));
        adminTableView.getColumns().addAll(adminUsernameTableColumn, adminEmailTableColumn);
        connectionHandler.admins.addListener((InvalidationListener) e -> {
            adminTableView.setItems(connectionHandler.admins);
        });
        Pane adminFillPane = new Pane();
        adminTableView.setItems(connectionHandler.admins);
        Button addAdminButton = new Button("Add");
        addAdminButton.setOnAction(e -> {
            new AddEditAdminDialog(stage, null, connectionHandler).showDialog();
        });
        Button editAdminButton = new Button("Edit");
        editAdminButton.setOnAction(e -> {
            if (!adminTableView.getSelectionModel().isEmpty()) {
                new AddEditAdminDialog(stage, adminTableView.getSelectionModel().getSelectedItem(), connectionHandler).showDialog();
            }
        });
        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(e -> {
            if (!adminTableView.getSelectionModel().isEmpty()) {
                connectionHandler.resetAdminPassword(adminTableView.getSelectionModel().getSelectedItem().getAdminName(), adminTableView.getSelectionModel().getSelectedItem().getAdminEmail());
            }

        });
        HBox adminButtonPane = new HBox(addAdminButton, editAdminButton, resetPasswordButton);
        adminButtonPane.setSpacing(10);
        adminButtonPane.setAlignment(Pos.CENTER);
        VBox adminPane = new VBox(adminTableView, adminFillPane, adminButtonPane);
        adminPane.setPadding(new Insets(50));
        adminPane.setSpacing(20);
        adminPane.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(adminFillPane, Priority.ALWAYS);
        //</editor-fold>

        //<editor-fold desc="Student Pane">
        //Setup student pane
        SearchPane studentSearchPane = new SearchPane();
        studentSearchPane.searches.addAll(connectionHandler.studentSearches);
        connectionHandler.studentSearches.addListener((InvalidationListener) e -> {
            Platform.runLater(() -> {
                studentSearchPane.searches.clear();
                studentSearchPane.searches.addAll(connectionHandler.studentSearches);
            });
        });
        studentSearchPane.searchListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (studentSearchPane.searchListView.getSelectionModel().getSelectedItem() != null) {
                connectionHandler.requestStudent(studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText());
            } else {
                connectionHandler.student.setStudent(null);
            }
        });
        studentSearchPane.addNewButton.setOnAction(e -> {
            new AddEditStudentDialog(stage, connectionHandler, null).showDialog();
        });
        TextArea studentInfoTextArea = new TextArea("Please select student");
        studentInfoTextArea.setEditable(false);

        Button editStudentButton = new Button("Edit Details");
        editStudentButton.setOnAction(e -> {
            if (connectionHandler.student.getStudent() != null) {
                new AddEditStudentDialog(stage, connectionHandler, connectionHandler.student.getStudent()).showDialog();
            }
        });
        Button removeStudentButton = new Button("Remove");
        removeStudentButton.setOnAction(e -> {
            if (connectionHandler.student.getStudent() != null) {
                connectionHandler.removeStudent();
            }
        });
        Button resetStudentPasswordButton = new Button("Reset Password");
        resetStudentPasswordButton.setOnAction(e -> {
            if (connectionHandler.student.getStudent() != null) {
                connectionHandler.resetStudentPassword();
            }
        });
        HBox studentButtonPane = new HBox(editStudentButton, removeStudentButton, resetStudentPasswordButton);
        studentButtonPane.setSpacing(15);

        Text studentClassText = new Text("Student Class");
        HBox studentClassTextPane = new HBox(studentClassText);
        studentClassTextPane.setAlignment(Pos.CENTER);
        ListView<ClassResultAttendance> studentClassListView = new ListView<>();
        studentClassListView.setMinWidth(300);
        studentClassListView.setPlaceholder(new Text("No student classes"));
        studentClassListView.setCellFactory(e -> new ListCell<ClassResultAttendance>() {
            @Override
            protected void updateItem(ClassResultAttendance item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    StudentClass studentClass = item.getStudentClass();
                    setText(studentClass.getClassID() + ": " + studentClass.getModuleNumber() + " - " + studentClass.getClassLecturer().getLastName());
                }
            }
        });
        Button addClassButton = new Button("Add");
        addClassButton.setOnAction(e -> {
            if (!studentSearchPane.searchListView.getSelectionModel().isEmpty()) {
                new AddClassDialog(stage, connectionHandler, studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText(), studentClassListView.getItems()).showDialog();
            }
        });
        Button removeStudentClassButton = new Button("Remove");
        removeStudentClassButton.setOnAction(e -> {
            if (!studentClassListView.getSelectionModel().isEmpty()) {
                connectionHandler.unregisterClass(studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText(), studentClassListView.getSelectionModel().getSelectedItem().getStudentClass().getClassID());
            }
        });
        HBox studentClassButtonPane = new HBox(addClassButton, removeStudentClassButton);
        studentClassButtonPane.setSpacing(10);
        studentClassButtonPane.setAlignment(Pos.CENTER);
        VBox studentClassPane = new VBox(studentClassTextPane, studentClassListView, studentClassButtonPane);
        studentClassPane.setSpacing(10);

        Text studentResultText = new Text("Student Result");
        HBox studentResultTextPane = new HBox(studentResultText);
        studentResultTextPane.setAlignment(Pos.CENTER);
        TableView<Result> studentResultTableView = new TableView<>();
        studentResultTableView.setMinWidth(300);
        studentResultTableView.setPlaceholder(new Text("No student results"));
        TableColumn<Result, String> studentResultNameTableColumn = new TableColumn<>("Result Name");
        studentResultNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("resultName"));
        TableColumn<Result, String> studentResultTableColumn = new TableColumn<>("Result");
        studentResultTableColumn.setCellValueFactory(p -> {
            if (p.getValue() != null) {
                return new SimpleStringProperty(String.format("%.0f / %.0f", p.getValue().getResult(), p.getValue().getResultMax()));
            } else {
                return new SimpleStringProperty("");
            }
        });
        studentResultTableView.getColumns().addAll(studentResultNameTableColumn, studentResultTableColumn);
        studentResultTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentClassListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            studentResultTableView.getItems().clear();
            if (studentClassListView.getSelectionModel().getSelectedItem() != null) {
                studentResultTableView.getItems().addAll(studentClassListView.getSelectionModel().getSelectedItem().getResults());
            }
        });
        Button editResultButton = new Button("Edit");
        editResultButton.setOnAction(e -> {
            if (!studentResultTableView.getSelectionModel().isEmpty()) {
                new EditResultDialog(stage, connectionHandler, studentResultTableView.getSelectionModel().getSelectedItem()).showDialog();
            }
        });
        Button regSuppExamButton = new Button("Reg Supp Exam");
        regSuppExamButton.setOnAction(e -> {
            if (!studentClassListView.getSelectionModel().isEmpty()) {
                connectionHandler.regSuppExam(studentResultTableView.getSelectionModel().getSelectedItem().getStudentNumber(), connectionHandler.studentClass.getStudentClass().getClassID());
            }
        });
        HBox studentResultButtonPane = new HBox(editResultButton, regSuppExamButton);
        studentResultButtonPane.setSpacing(10);
        studentResultButtonPane.setAlignment(Pos.CENTER);
        VBox studentResultPane = new VBox(studentResultTextPane, studentResultTableView, studentResultButtonPane);
        studentResultPane.setSpacing(10);

        Text studentAttendanceText = new Text("Student Attendance");
        HBox studentAttendanceTextPane = new HBox(studentAttendanceText);
        studentAttendanceTextPane.setAlignment(Pos.CENTER);
        TableView<Attendance> studentAttendanceTableView = new TableView<>();
        studentAttendanceTableView.setMinWidth(300);
        studentAttendanceTableView.setPlaceholder(new Text("No student attendance"));
        TableColumn<Attendance, String> studentAttendanceDateTableColumn = new TableColumn<>("Date");
        studentAttendanceDateTableColumn.setCellValueFactory(new PropertyValueFactory<>("attendanceDate"));
        TableColumn<Attendance, String> studentAttendanceTableColumn = new TableColumn<>("Attendance");
        studentAttendanceTableColumn.setCellValueFactory(p -> {
            if (p.getValue() != null) {
                if (p.getValue().getAttendance().equals("P")) {
                    return new SimpleStringProperty("Present");
                } else if (p.getValue().getAttendance().equals("L")) {
                    return new SimpleStringProperty("Late");
                } else if (p.getValue().getAttendance().equals("A")) {
                    return new SimpleStringProperty("Absent");
                } else if (p.getValue().getAttendance().equals("E")) {
                    return new SimpleStringProperty("Left Early");
                }
            }
            return new SimpleStringProperty("");
        });
        studentAttendanceTableView.getColumns().addAll(studentAttendanceDateTableColumn, studentAttendanceTableColumn);
        studentAttendanceTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentClassListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            studentAttendanceTableView.getItems().clear();
            if (studentClassListView.getSelectionModel().getSelectedItem() != null) {
                studentAttendanceTableView.getItems().addAll(studentClassListView.getSelectionModel().getSelectedItem().getAttendance());
            }
        });
        Button editAttendanceButton = new Button("Edit");
        editAttendanceButton.setOnAction(e -> {
            if (!studentAttendanceTableView.getSelectionModel().isEmpty()) {
                new EditAttendanceDialog(stage, connectionHandler, studentAttendanceTableView.getSelectionModel().getSelectedItem());
            }
        });
        Button removeAttendanceButton = new Button("Remove");
        removeAttendanceButton.setOnAction(e -> {
            if (!studentAttendanceTableView.getSelectionModel().isEmpty()) {
                connectionHandler.removeAttendance(studentAttendanceTableView.getSelectionModel().getSelectedItem().getAttendanceID());
            }
        });
        HBox studentAttendanceButtonPane = new HBox(editAttendanceButton, removeAttendanceButton);
        studentAttendanceButtonPane.setSpacing(10);
        studentAttendanceButtonPane.setAlignment(Pos.CENTER);
        VBox studentAttendancePane = new VBox(studentAttendanceTextPane, studentAttendanceTableView, studentAttendanceButtonPane);
        studentAttendancePane.setSpacing(10);

        HBox studentDataPane = new HBox(studentClassPane, studentResultPane, studentAttendancePane);
        studentDataPane.setAlignment(Pos.CENTER);
        studentDataPane.setSpacing(25);

        VBox studentInfoPane = new VBox(studentInfoTextArea, studentButtonPane, studentDataPane);
        studentInfoPane.setPadding(new Insets(20));
        studentInfoPane.setSpacing(25);
        studentInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(studentInfoPane, Priority.ALWAYS);

        HBox studentPane = new HBox(studentSearchPane, studentInfoPane);

        connectionHandler.student.updated.addListener(e -> {
            if (connectionHandler.student.updated.get()) {
                Platform.runLater(() -> {
                    studentClassListView.getItems().clear();
                    if (connectionHandler.student.getStudent() != null) {
                        studentInfoTextArea.setText(connectionHandler.student.getStudent().getStudentInformation());
                        studentClassListView.setItems(FXCollections.observableArrayList(connectionHandler.student.getStudent().getClassResultAttendances()));
                    } else {
                        studentInfoTextArea.setText("Please select student");
                    }
                });
                connectionHandler.student.updated.set(false);
            }
        });
        //</editor-fold>

        //<editor-fold desc="Lecturer Pane">
        //Setup lecturer pane
        SearchPane lecturerSearchPane = new SearchPane();
        lecturerSearchPane.searches.addAll(connectionHandler.lecturerSearches);
        connectionHandler.lecturerSearches.addListener((InvalidationListener) e -> {
            Platform.runLater(() -> {
                lecturerSearchPane.searches.clear();
                lecturerSearchPane.searches.addAll(connectionHandler.lecturerSearches);
            });
        });
        lecturerSearchPane.searchListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (lecturerSearchPane.searchListView.getSelectionModel().getSelectedItem() != null) {
                connectionHandler.requestLecturer(lecturerSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText());
            } else {
                connectionHandler.lecturer.setLecturer(null);
            }
        });
        lecturerSearchPane.addNewButton.setOnAction(e -> {
            new AddEditLecturerDialog(stage, connectionHandler, null).showDialog();
        });

        Circle lecturerPicture = new Circle(30);
        lecturerPicture.setStroke(Color.BLACK);
        lecturerPicture.setStrokeWidth(2);
        lecturerPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
        TextArea lecturerInfoTextArea = new TextArea("Please select lecturer");
        lecturerInfoTextArea.setEditable(false);
        HBox lecturerTopPane = new HBox(lecturerPicture, lecturerInfoTextArea);
        lecturerTopPane.setSpacing(15);

        Button editLecturerButton = new Button("Edit");
        editLecturerButton.setOnAction(e -> {
            if (connectionHandler.lecturer.getLecturer() != null) {
                new AddEditLecturerDialog(stage, connectionHandler, connectionHandler.lecturer.getLecturer()).showDialog();
            }
        });
        Button removeLecturerButton = new Button("Remove");
        removeLecturerButton.setOnAction(e -> {
            if (connectionHandler.lecturer.getLecturer() != null) {
                connectionHandler.removeLecturer();
            }
        });
        Button resetLecturerPasswordButton = new Button("Reset Password");
        resetLecturerPasswordButton.setOnAction(e -> {
            if (connectionHandler.lecturer.getLecturer() != null) {
                connectionHandler.resetLecturerPassword();
            }
        });
        HBox lecturerButtonPane = new HBox(editLecturerButton, removeLecturerButton, resetLecturerPasswordButton);
        lecturerButtonPane.setSpacing(15);

        Text lecturerClassText = new Text("Lecturer Class");
        HBox lecturerClassTextPane = new HBox(lecturerClassText);
        lecturerClassTextPane.setAlignment(Pos.CENTER);
        ListView<LecturerClass> lecturerClassListView = new ListView<>();
        lecturerClassListView.setMaxWidth(600);
        lecturerClassListView.setPlaceholder(new Text("No lecturer classes"));
        lecturerClassListView.setCellFactory(e -> new ListCell<LecturerClass>() {
            @Override
            protected void updateItem(LecturerClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getId() + ": " + item.getModuleNumber());
                }
            }
        });
        VBox lecturerClassPane = new VBox(lecturerClassTextPane, lecturerClassListView);
        studentClassPane.setSpacing(10);
        lecturerClassPane.setAlignment(Pos.TOP_CENTER);
        connectionHandler.lecturer.updated.addListener(e -> {
            Platform.runLater(() -> {
                if (connectionHandler.lecturer.updated.get()) {
                    lecturerClassListView.getItems().clear();
                    if (connectionHandler.lecturer.getLecturer() != null) {
                        lecturerClassListView.getItems().addAll(connectionHandler.lecturer.getLecturer().getClasses());
                        if (connectionHandler.lecturer.getLecturer().getImage() != null) {
                            lecturerPicture.setFill(new ImagePattern(connectionHandler.lecturer.getLecturer().getImage()));
                        } else {
                            lecturerPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
                        }
                        lecturerInfoTextArea.setText(connectionHandler.lecturer.getLecturer().getLecturerDetails());
                    } else {
                        lecturerInfoTextArea.setText("Please select lecturer");
                        lecturerPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
                    }
                    connectionHandler.lecturer.updated.set(false);
                }
            });
        });

        VBox lecturerInfoPane = new VBox(lecturerTopPane, lecturerButtonPane, lecturerClassPane);
        lecturerInfoPane.setPadding(new Insets(20));
        lecturerInfoPane.setSpacing(25);
        lecturerInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(lecturerInfoPane, Priority.ALWAYS);
        HBox lecturerPane = new HBox(lecturerSearchPane, lecturerInfoPane);
        //</editor-fold>

        //<editor-fold desc="Class Pane">
        //Setup Class
        SearchPane classSearchPane = new SearchPane();
        classSearchPane.searches.addAll(connectionHandler.classSearches);
        connectionHandler.classSearches.addListener((InvalidationListener) e -> {
            Platform.runLater(() -> {
                classSearchPane.searches.clear();
                classSearchPane.searches.addAll(connectionHandler.classSearches);
            });
        });
        classSearchPane.searchListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (classSearchPane.searchListView.getSelectionModel().getSelectedItem() != null) {
                connectionHandler.requestClass(classSearchPane.searchListView.getSelectionModel().getSelectedItem().getPrimaryText());
            } else {
                connectionHandler.studentClass.setStudentClass(null);
            }
        });
        classSearchPane.addNewButton.setOnAction(e -> {
            //TODO
        });

        TextArea classInfoTextArea = new TextArea("Please select class");
        classInfoTextArea.setEditable(false);

        Button editClassButton = new Button("Edit Details");
        editClassButton.setOnAction(e -> {
            //TODO
        });
        Button removeClassButton = new Button("Remove");
        removeClassButton.setOnAction(e -> {
            if (connectionHandler.studentClass.getStudentClass() != null) {
                connectionHandler.removeClass();
            }
        });
        HBox classButtonPane = new HBox(editClassButton, removeClassButton);
        classButtonPane.setSpacing(15);


        Text classTimesText = new Text("Class Times");
        HBox classTimesTextPane = new HBox(classTimesText);
        classTimesTextPane.setAlignment(Pos.CENTER);

        TableView<ClassTime> classTimeTableView = new TableView<>();
        classTimeTableView.setMinWidth(450);
        classTimeTableView.setPlaceholder(new Text("No student results"));
        TableColumn<ClassTime, String> classTimeDayTableColumn = new TableColumn<>("Class Day");
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        classTimeDayTableColumn.setCellValueFactory(p -> {
            if (p.getValue() != null) {
                return new SimpleStringProperty(weekdays[p.getValue().getDayOfWeek() - 1]);
            } else {
                return new SimpleStringProperty("");
            }
        });
        TableColumn<ClassTime, String> classTimeTableColumn = new TableColumn<>("Class Time");
        String[] startTimeSlots = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "18:45", "19:30"};
        String[] endTimeSlots = {"08:45", "09:45", "10:45", "11:45", "12:45", "13:45", "14:45", "15:45", "16:45", "17:45", "18:45", "19:30", "20:15"};
        classTimeTableColumn.setCellValueFactory(p -> {
            if (p.getValue() != null) {
                return new SimpleStringProperty(startTimeSlots[p.getValue().getStartSlot()] + " - " + endTimeSlots[p.getValue().getEndSlot()]);
            } else {
                return new SimpleStringProperty("");
            }
        });
        TableColumn<ClassTime, String> classTimeLocationTableColumn = new TableColumn<>("Classroom");
        classTimeLocationTableColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        classTimeTableView.getColumns().addAll(classTimeDayTableColumn, classTimeTableColumn, classTimeLocationTableColumn);
        classTimeTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Button addClassTimeButton = new Button("Add");
        addClassTimeButton.setOnAction(e -> {
            if (connectionHandler.studentClass.getStudentClass() != null) {
                new AddClassTimeDialog(stage, connectionHandler, connectionHandler.studentClass.getStudentClass().getClassID()).showDialog();
            }
        });
        Button removeClassTimeButton = new Button("Remove");
        removeClassTimeButton.setOnAction(e -> {
            if (!classTimeTableView.getSelectionModel().isEmpty()) {
                connectionHandler.removeClassTime(classTimeTableView.getSelectionModel().getSelectedItem().getId());
            }
        });
        HBox classTimeButtonPane = new HBox(addClassTimeButton, removeClassTimeButton);
        classTimeButtonPane.setSpacing(10);
        classTimeButtonPane.setAlignment(Pos.CENTER);
        VBox classTimePane = new VBox(classTimesTextPane, classTimeTableView, classTimeButtonPane);
        classTimePane.setSpacing(10);

        Text classResultTemplateText = new Text("Result Template");
        HBox classResultTemplateTextPane = new HBox(classResultTemplateText);
        classResultTemplateTextPane.setAlignment(Pos.CENTER);

        TableView<ResultTemplate> classResultTemplateTableView = new TableView<>();
        classResultTemplateTableView.setMinWidth(450);
        classResultTemplateTableView.setPlaceholder(new Text("No result templates"));
        TableColumn<ResultTemplate, String> classResultTemplateNameTableColumn = new TableColumn<>("Result Name");
        classResultTemplateNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("resultName"));
        TableColumn<ResultTemplate, String> classResultTemplateMaxTableColumn = new TableColumn<>("Result Max");
        classResultTemplateMaxTableColumn.setCellValueFactory(new PropertyValueFactory<>("resultMax"));
        TableColumn<ResultTemplate, String> classResultTemplateDPTableColumn = new TableColumn<>("DP Weight");
        classResultTemplateDPTableColumn.setCellValueFactory(new PropertyValueFactory<>("dpWeight"));
        TableColumn<ResultTemplate, String> classResultTemplateFinalTableColumn = new TableColumn<>("Final Weight");
        classResultTemplateFinalTableColumn.setCellValueFactory(new PropertyValueFactory<>("finalWeight"));
        classResultTemplateTableView.getColumns().addAll(classResultTemplateNameTableColumn, classResultTemplateMaxTableColumn, classResultTemplateDPTableColumn, classResultTemplateFinalTableColumn);
        classResultTemplateTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Button editResultTemplateButton = new Button("Edit");
        editResultTemplateButton.setOnAction(e -> {
            //TODO

        });
        HBox classResultTemplateButtonPane = new HBox(editResultTemplateButton);
        classResultTemplateButtonPane.setSpacing(10);
        classResultTemplateButtonPane.setAlignment(Pos.CENTER);
        VBox classResultTemplatePane = new VBox(classResultTemplateTextPane, classResultTemplateTableView, classResultTemplateButtonPane);
        classResultTemplatePane.setSpacing(10);

        HBox classDataPane = new HBox(classTimePane, classResultTemplatePane);
        classDataPane.setSpacing(25);
        classDataPane.setAlignment(Pos.TOP_CENTER);

        VBox classInfoPane = new VBox(classInfoTextArea, classButtonPane, classDataPane);
        classInfoPane.setPadding(new Insets(20));
        classInfoPane.setSpacing(25);
        classInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(classInfoPane, Priority.ALWAYS);

        HBox classPane = new HBox(classSearchPane, classInfoPane);

        connectionHandler.studentClass.updated.addListener(e -> {
            if (connectionHandler.studentClass.updated.get()) {
                Platform.runLater(() -> {
                    classTimeTableView.getItems().clear();
                    classResultTemplateTableView.getItems().clear();
                    if (connectionHandler.studentClass.getStudentClass() != null) {
                        classInfoTextArea.setText(connectionHandler.studentClass.getStudentClass().getClassDetails());
                        classTimeTableView.setItems(FXCollections.observableArrayList(connectionHandler.studentClass.getStudentClass().getClassTimes()));
                        classResultTemplateTableView.setItems(FXCollections.observableArrayList(connectionHandler.studentClass.getStudentClass().getResultTemplates()));
                    } else {
                        classInfoTextArea.setText("Please select class");
                    }
                    connectionHandler.studentClass.updated.set(false);
                });
            }
        });
        //</editor-fold>

        //<editor-fold desc="Contact Pane">
        //Setup Contact Pane
        SearchPane contactSearchPane = new SearchPane();
        contactSearchPane.searches.addAll(connectionHandler.contactSearches);
        connectionHandler.contactSearches.addListener((InvalidationListener) e -> {
            Platform.runLater(() -> {
                contactSearchPane.searches.clear();
                contactSearchPane.searches.addAll(connectionHandler.contactSearches);
            });
        });
        contactSearchPane.searchListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (contactSearchPane.searchListView.getSelectionModel().getSelectedItem() != null) {
                connectionHandler.requestContact(contactSearchPane.searchListView.getSelectionModel().getSelectedItem().getPrimaryText(), contactSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText());
            } else {
                connectionHandler.contactDetails.setContactDetails(null);
            }
        });
        contactSearchPane.addNewButton.setOnAction(e -> {
            new AddEditContactDialog(stage, connectionHandler, null).showDialog();
        });

        Circle contactPicture = new Circle(30);
        contactPicture.setStroke(Color.BLACK);
        contactPicture.setStrokeWidth(2);
        contactPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
        TextArea contactInfoTextArea = new TextArea("Please select contact");
        contactInfoTextArea.setEditable(false);
        HBox contactTopPane = new HBox(contactPicture, contactInfoTextArea);
        contactTopPane.setSpacing(15);

        Button editContactButton = new Button("Edit Details");
        editContactButton.setOnAction(e -> {
            if (connectionHandler.contactDetails.getContactDetails() != null) {
                new AddEditContactDialog(stage, connectionHandler, connectionHandler.contactDetails.getContactDetails()).showDialog();
            }
        });
        Button removeContactButton = new Button("Remove");
        removeContactButton.setOnAction(e -> {
            if (connectionHandler.contactDetails.getContactDetails() != null) {
                connectionHandler.removeContact();
            }
        });
        HBox contactButtonPane = new HBox(editContactButton, removeContactButton);
        contactButtonPane.setSpacing(15);

        VBox contactInfoPane = new VBox(contactTopPane, contactButtonPane);
        contactInfoPane.setPadding(new Insets(20));
        contactInfoPane.setSpacing(25);
        contactInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(contactInfoPane, Priority.ALWAYS);

        HBox contactPane = new HBox(contactSearchPane, contactInfoPane);

        connectionHandler.contactDetails.updated.addListener(e -> {
            if (connectionHandler.contactDetails.updated.get()) {
                Platform.runLater(() -> {
                    contactPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
                    if (connectionHandler.contactDetails.getContactDetails() != null) {
                        contactInfoTextArea.setText(connectionHandler.contactDetails.getContactDetails().getContactDetails());
                        if (connectionHandler.contactDetails.getContactDetails().getImage() != null) {
                            contactPicture.setFill(new ImagePattern(connectionHandler.contactDetails.getContactDetails().getImage()));
                        }
                    } else {
                        contactInfoTextArea.setText("Please select contact details");
                    }
                    connectionHandler.contactDetails.updated.set(false);
                });
            }
        });
        //</editor-fold>

        //<editor-fold desc="Notice Pane">
        //Setup notice pane
        TableView<Notice> noticeTableView = new TableView<>();
        noticeTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Notice, String> noticeIDTableColumn = new TableColumn<>("ID");
        noticeIDTableColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        //noticeIDTableColumn.setResizable(false);
        //noticeIDTableColumn.setMaxWidth(50);
        TableColumn<Notice, String> noticeHeadingTableColumn = new TableColumn<>("Heading");
        noticeHeadingTableColumn.setCellValueFactory(new PropertyValueFactory<>("heading"));
        TableColumn<Notice, String> noticeDescriptionTableColumn = new TableColumn<>("Description");
        noticeDescriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Notice, String> noticeTagTableColumn = new TableColumn<>("Tag");
        noticeTagTableColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
        TableColumn<Notice, String> noticeExpiryTableColumn = new TableColumn<>("ExpiryDate");
        noticeExpiryTableColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        noticeTableView.getColumns().addAll(noticeIDTableColumn, noticeHeadingTableColumn, noticeDescriptionTableColumn, noticeTagTableColumn, noticeExpiryTableColumn);
        connectionHandler.notices.addListener((InvalidationListener) e -> {
            noticeTableView.setItems(connectionHandler.notices);
        });
        Button addNoticeButton = new Button("Add");
        addNoticeButton.setOnAction(e -> {
            new AddEditNoticeDialog(stage, connectionHandler, null).showDialog();
        });
        Button editNoticeButton = new Button("Edit");
        editNoticeButton.setOnAction(e -> {
            if (!noticeTableView.getSelectionModel().isEmpty()) {
                new AddEditNoticeDialog(stage, connectionHandler, noticeTableView.getSelectionModel().getSelectedItem()).showDialog();
            }
        });
        Button removeNoticeButton = new Button("Remove");
        removeNoticeButton.setOnAction(e -> {
            if (!noticeTableView.getSelectionModel().isEmpty()) {
                connectionHandler.removeNotice(noticeTableView.getSelectionModel().getSelectedItem().getId());
            }
        });
        HBox noticeButtonPane = new HBox(addNoticeButton, editNoticeButton, removeNoticeButton);
        noticeButtonPane.setSpacing(15);
        noticeButtonPane.setAlignment(Pos.CENTER);
        VBox noticePane = new VBox(noticeTableView, noticeButtonPane);
        noticePane.setPadding(new Insets(20));
        noticePane.setSpacing(20);
        noticePane.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(noticeTableView, Priority.ALWAYS);
        //</editor-fold>

        //<editor-fold desc="Notification Pane">
        //Setup notification pane
        TableView<Notification> notificationTableView = new TableView<>();
        notificationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Notification, String> notificationIDTableColumn = new TableColumn<>("ID");
        notificationIDTableColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        //notificationIDTableColumn.setResizable(false);
        //notificationIDTableColumn.setMaxWidth(50);
        TableColumn<Notification, String> notificationHeadingTableColumn = new TableColumn<>("Heading");
        notificationHeadingTableColumn.setCellValueFactory(new PropertyValueFactory<>("heading"));
        TableColumn<Notification, String> notificationDescriptionTableColumn = new TableColumn<>("Description");
        notificationDescriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Notification, String> notificationTagTableColumn = new TableColumn<>("Tag");
        notificationTagTableColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
        notificationTableView.getColumns().addAll(notificationIDTableColumn, notificationHeadingTableColumn, notificationDescriptionTableColumn, notificationTagTableColumn);
        connectionHandler.notices.addListener((InvalidationListener) e -> {
            notificationTableView.setItems(connectionHandler.notifications);
        });
        Button addNotificationButton = new Button("Add");
        addNotificationButton.setOnAction(e -> {
            new AddEditNotificationDialog(stage, connectionHandler, null).showDialog();
        });
        Button editNotificationButton = new Button("Edit");
        editNotificationButton.setOnAction(e -> {
            if (!notificationTableView.getSelectionModel().isEmpty()) {
                new AddEditNotificationDialog(stage, connectionHandler, notificationTableView.getSelectionModel().getSelectedItem()).showDialog();
            }
        });
        Button removeNotificationButton = new Button("Remove");
        removeNotificationButton.setOnAction(e -> {
            if (!notificationTableView.getSelectionModel().isEmpty()) {
                connectionHandler.removeNotification(notificationTableView.getSelectionModel().getSelectedItem().getId());
            }
        });
        HBox notificationButtonPane = new HBox(addNotificationButton, editNotificationButton, removeNotificationButton);
        notificationButtonPane.setSpacing(15);
        notificationButtonPane.setAlignment(Pos.CENTER);
        VBox notificationPane = new VBox(notificationTableView, notificationButtonPane);
        notificationPane.setPadding(new Insets(20));
        notificationPane.setSpacing(20);
        notificationPane.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(notificationTableView, Priority.ALWAYS);
        //</editor-fold>

        //<editor-fold desc="Dates Pane">
        //Setup dates pane
        TableView<ImportantDate> datesTableView = new TableView<>();
        datesTableView.setMaxWidth(500);
        datesTableView.setMaxHeight(600);
        TableColumn<ImportantDate, String> dateTableColumn = new TableColumn<>("Date");
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateTableColumn.prefWidthProperty().bind(datesTableView.widthProperty().multiply(0.2));
        dateTableColumn.setResizable(false);
        TableColumn<ImportantDate, String> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionTableColumn.prefWidthProperty().bind(datesTableView.widthProperty().multiply(0.8).subtract(2));
        descriptionTableColumn.setResizable(false);
        datesTableView.getColumns().addAll(dateTableColumn, descriptionTableColumn);
        connectionHandler.importantDates.addListener((InvalidationListener) e -> {
            datesTableView.setItems(connectionHandler.importantDates);
        });
        Button addDateButton = new Button("Add");
        addDateButton.setOnAction(e -> {
            new AddEditDateDialog(stage, connectionHandler, null).showDialog();
        });
        Button editDateButton = new Button("Edit");
        editDateButton.setOnAction(e -> {
            if (!datesTableView.getSelectionModel().isEmpty()) {
                new AddEditDateDialog(stage, connectionHandler, datesTableView.getSelectionModel().getSelectedItem()).showDialog();
            }
        });
        Button removeDateButton = new Button("Remove");
        removeDateButton.setOnAction(e -> {
            if (!datesTableView.getSelectionModel().isEmpty()) {
                connectionHandler.removeDate(datesTableView.getSelectionModel().getSelectedItem().getId());
            }
        });
        HBox dateButtonPane = new HBox(addDateButton, editDateButton, removeDateButton);
        dateButtonPane.setSpacing(15);
        dateButtonPane.setAlignment(Pos.CENTER);
        VBox datesPane = new VBox(datesTableView, dateButtonPane);
        datesPane.setPadding(new Insets(20));
        datesPane.setSpacing(20);
        datesPane.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(datesTableView, Priority.ALWAYS);
        //</editor-fold>

        //<editor-fold desc="Log Pane">
        //Setup log pane
        ListView<String> logListView = new ListView<>();
        Label searchResultsLabel = new Label("No matches");
        Label searchResultIndexLabel = new Label();
        ObservableList<Integer> searchIndexes = FXCollections.observableArrayList();
        IntegerProperty searchIndex = new SimpleIntegerProperty(-1);
        searchIndex.addListener(e -> {
            if (searchIndex.get() > -1) {
                searchResultIndexLabel.setText((searchIndex.get() + 1) + "");
                logListView.getSelectionModel().select(searchIndexes.get(searchIndex.get()));
                logListView.scrollTo(searchIndexes.get(searchIndex.get()));
            } else {
                searchResultIndexLabel.setText("");
            }
        });
        TextField logSearchTextField = new TextField();
        logSearchTextField.setPromptText("Search");
        logSearchTextField.textProperty().addListener(e -> {
            searchIndexes.clear();
            searchIndex.set(-1);
            ObservableList<String> logList = logListView.getItems();
            if (!logSearchTextField.getText().isEmpty()) {
                for (int i = 0; i < logList.size(); i++) {
                    if (logList.get(i).toLowerCase().contains(logSearchTextField.getText().toLowerCase())) {
                        searchIndexes.add(i);
                    }
                }
            }
            if (!searchIndexes.isEmpty()) {
                searchIndex.set(0);
                searchResultsLabel.setText(searchIndexes.size() + " matches found");
            } else {
                searchIndex.set(-1);
                searchResultsLabel.setText("No matches");
            }
        });
        logListView.itemsProperty().addListener(e -> {
            String searchText = logSearchTextField.getText();
            logSearchTextField.setText("");
            logSearchTextField.setText(searchText);
        });
        logListView.setFocusTraversable(false);
        logListView.setPlaceholder(new Label("Press refresh to load log"));
        logListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            event.consume();
        });
        Button previousButton = new Button("<");
        previousButton.setOnAction(e -> searchIndex.set(Math.floorMod(searchIndex.get() - 1, searchIndexes.size())));
        Button nextButton = new Button(">");
        nextButton.setOnAction(e -> searchIndex.set(Math.floorMod(searchIndex.get() + 1, searchIndexes.size())));
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> connectionHandler.requestLogFile());
        Button exportButton = new Button("Export");
        exportButton.setOnAction(e -> {
            try {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Export log file to..");
                File destDirectory = directoryChooser.showDialog(stage);
                if (destDirectory.exists()) {
                    File destFile = new File(destDirectory.getAbsolutePath() + "/Log File.txt");
                    Files.write(destFile.toPath(), connectionHandler.adminLog.getAdminLog().getLogFile());
                    System.out.println("Exported file");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        HBox topPane = new HBox(logSearchTextField, searchResultsLabel, previousButton, searchResultIndexLabel, nextButton, refreshButton, exportButton);
        topPane.setSpacing(10);
        topPane.setAlignment(Pos.CENTER);
        connectionHandler.adminLog.updated.addListener(e -> {
            if (connectionHandler.adminLog.updated.get()) {
                ObservableList<String> log = FXCollections.observableArrayList();
                try {
                    File tempFile = new File(APPLICATION_FOLDER.getAbsolutePath() + "/tmp.txt");
                    Files.write(tempFile.toPath(), connectionHandler.adminLog.getAdminLog().getLogFile());
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));
                    final String[] line = new String[1];
                    while ((line[0] = bufferedReader.readLine()) != null) {
                        log.add(line[0]);
                    }
                    tempFile.delete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(() -> logListView.setItems(log));
                connectionHandler.adminLog.updated.set(false);
            }
        });
        VBox logPane = new VBox(topPane, logListView);
        VBox.setVgrow(logListView, Priority.ALWAYS);
        logPane.setPadding(new Insets(10));
        logPane.setSpacing(10);
        logPane.setAlignment(Pos.CENTER);
        //</editor-fold>

        //<editor-fold desc="Tab Pane">
        //Setup tab pane
        Tab adminTab = new Tab("Admin", adminPane);
        Tab studentTab = new Tab("Student", studentPane);
        Tab lecturerTab = new Tab("Lecturer", lecturerPane);
        Tab classTab = new Tab("Class", classPane);
        Tab contactTab = new Tab("Contact Details", contactPane);
        Tab noticeTab = new Tab("Notices", noticePane);
        Tab notificationTab = new Tab("Notifications", notificationPane);
        Tab datesTab = new Tab("Important Dates", datesPane);
        Tab logTab = new Tab("Server Log", logPane);
        TabPane tabPane = new TabPane(adminTab, studentTab, lecturerTab, classTab, contactTab, noticeTab, notificationTab, datesTab, logTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        //</editor-fold>

        //<editor-fold desc="Scene">
        //Setup scene
        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("CampusLiveStyle.css").toExternalForm());
        //</editor-fold>

        //<editor-fold desc="Start Stage">
        //Select and show scene
        stage.setScene(scene);
        stage.show();
        //</editor-fold>

        //<editor-fold desc="Change Default Password">
        if (connectionHandler.isDefaultPassword()) {
            new ChangePasswordDialog(stage, connectionHandler).showDialog();
        }
        //</editor-fold>
    }

    public static void main(String[] args) {
        launch(null);
    }
}
