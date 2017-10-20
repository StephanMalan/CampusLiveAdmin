package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.all.ClassResultAttendance;
import models.all.StudentClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class Display extends Application{

    public static final File APPLICATION_FOLDER = new File(System.getProperty("user.home") + "/AppData/Local/Swooosh/CampusLiveAdmin");
    private ConnectionHandler connectionHandler = new ConnectionHandler();

    public void start(Stage stage) throws Exception {

        Login login = new Login(connectionHandler);
        try {
            login.start(new Stage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!login.loggedIn) {
            System.exit(0);
        }

        //Setup stage
        stage.setMaximized(true);
        stage.setTitle("CampusLiveAdmin");
        stage.setOnCloseRequest(e -> System.exit(0));

        //Test
        VBox vBox = new VBox(new Label("Test!"));

        //Setup student tab
        SearchPane studentSearchPane = new SearchPane();
        studentSearchPane.searches.addAll(connectionHandler.studentSearches);
        connectionHandler.studentSearches.addListener((InvalidationListener) e -> {
            Platform.runLater(() -> {
                studentSearchPane.searches.clear();
                studentSearchPane.searches.addAll(connectionHandler.studentSearches);
            });
        });
        studentSearchPane.searchListView.getSelectionModel().selectedItemProperty().addListener(e -> {
            connectionHandler.requestStudent(studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText());
        });
        studentSearchPane.addNewButton.setOnAction(e -> {
            //TODO popup
        });
        TextArea studentInfoTextArea = new TextArea("");
        studentInfoTextArea.setEditable(false);
        ListView<ClassResultAttendance> studentClassListView = new ListView<>();
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
        Button addClassButton = new Button("Add Class");
        addClassButton.setOnAction(e -> {
            //TODO add class
            if (!studentSearchPane.searchListView.getSelectionModel().isEmpty()) {
                new AddClassDialog(stage, connectionHandler, studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText(), studentClassListView.getItems()).showDialog();
            }
        });
        Button removeClassButton = new Button("Remove Class");
        removeClassButton.setOnAction(e -> {
            if (!studentClassListView.getSelectionModel().isEmpty()) {
                connectionHandler.unregisterClass(studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText(), studentClassListView.getSelectionModel().getSelectedItem().getStudentClass().getClassID());
            }
        });
        HBox studentClassButtonPane = new HBox(addClassButton, removeClassButton);
        studentClassButtonPane.setSpacing(10);
        studentClassButtonPane.setAlignment(Pos.CENTER);
        VBox studentClassPane = new VBox(studentClassListView, studentClassButtonPane);
        studentClassPane.setSpacing(10);
        HBox studentDataPane = new HBox(studentClassPane);
        studentDataPane.setAlignment(Pos.CENTER);
        VBox studentInfoPane = new VBox(studentInfoTextArea, studentDataPane);
        studentInfoPane.setPadding(new Insets(10));
        studentInfoPane.setSpacing(10);
        studentInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(studentInfoPane, Priority.ALWAYS);
        HBox studentPane = new HBox(studentSearchPane, studentInfoPane);
        connectionHandler.student.updated.addListener(e -> {
            if (connectionHandler.student.updated.get()) {
                Platform.runLater(() -> studentClassListView.getItems().clear());
                if (connectionHandler.student.getStudent() != null) {
                    Platform.runLater(() -> {
                        studentInfoTextArea.setText(connectionHandler.student.getStudent().toString());
                        studentClassListView.setItems(FXCollections.observableArrayList(connectionHandler.student.getStudent().getClassResultAttendances()));
                    });
                } else {
                    studentInfoTextArea.setText("Please select student");
                }
                connectionHandler.student.updated.set(false);
            }
        });

        //Setup log tab
        TextField logSearchTextField = new TextField();
        logSearchTextField.setPromptText("Search");
        logSearchTextField.textProperty().addListener(e -> {
            if (connectionHandler.adminLog.getAdminLog() == null) {
                connectionHandler.requestLogFile();
            }
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> connectionHandler.requestLogFile());
        HBox topPane = new HBox(logSearchTextField, refreshButton);
        topPane.setSpacing(10);
        topPane.setAlignment(Pos.CENTER);
        TextArea logTextArea = new TextArea();
        //logTextArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        connectionHandler.adminLog.updated.addListener(e -> {
            Platform.runLater(() -> logTextArea.setText(""));
            try {
                File tempFile = new File(APPLICATION_FOLDER.getAbsolutePath() + "/tmp.txt");
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                fileOutputStream.write(connectionHandler.adminLog.getAdminLog().getLogFile());
                fileOutputStream.close();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));
                final String[] line = new String[1];
                while ((line[0] = bufferedReader.readLine()) != null) {
                    Platform.runLater(() -> logTextArea.appendText(line[0] + "\n"));
                }
                tempFile.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        ScrollPane logScrollPane = new ScrollPane(logTextArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setFitToHeight(true);
        //prefWidthProperty().bind(logTextArea.prefWidthProperty());
        //logScrollPane.prefHeightProperty().bind(logTextArea.prefHeightProperty());
        VBox logPane = new VBox(topPane, logScrollPane);
        VBox.setVgrow(logScrollPane, Priority.ALWAYS);
        logPane.setPadding(new Insets(10));
        logPane.setSpacing(10);
        logPane.setAlignment(Pos.CENTER);

        //Setup tab pane
        Tab adminTab = new Tab("Admin", vBox);
        Tab studentTab = new Tab("Student", studentPane);
        Tab lecturerTab = new Tab("Lecturer", vBox);
        Tab classTab = new Tab("Class", vBox);
        Tab contactTab = new Tab("Contact Details", vBox);
        Tab noticeTab = new Tab("Notices", vBox);
        Tab notificationTab = new Tab("Notifications", vBox);
        Tab datesTab = new Tab("Important Dates", vBox);
        Tab logTab = new Tab("Server Log", logPane);
        TabPane tabPane = new TabPane(adminTab, studentTab, lecturerTab, classTab, contactTab, noticeTab, notificationTab, datesTab, logTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //Setup scene
        Scene scene = new Scene(tabPane);

        //Select and show scene
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(null);
    }
}
