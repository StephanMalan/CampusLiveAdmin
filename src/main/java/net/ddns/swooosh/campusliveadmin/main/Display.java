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

public class Display extends Application{

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

        //Test
        VBox vBox = new VBox(new Label("Test!"));

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
        Button addAdminButton = new Button("Add Admin");
        addAdminButton.setOnAction(e -> {
            //TODO
        });
        Button editAdminButton = new Button("Edit Admin");
        editAdminButton.setOnAction(e -> {
            //TODO
        });
        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(e -> {
            //TODO email broken on server
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
                connectionHandler.requestStudent(studentSearchPane.searchListView.getSelectionModel().getSelectedItem().getSecondaryText()); //TODO did throw nullpointer
            } else {
                connectionHandler.student.setStudent(null);
            }
        });
        studentSearchPane.addNewButton.setOnAction(e -> {
            //TODO popup
        });
        TextArea studentInfoTextArea = new TextArea("");
        studentInfoTextArea.setEditable(false);

        Text studentClassText = new Text("Student Class");
        HBox studentClassTextPane = new HBox(studentClassText);
        studentClassTextPane.setAlignment(Pos.CENTER);
        ListView<ClassResultAttendance> studentClassListView = new ListView<>();
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
        VBox studentClassPane = new VBox(studentClassTextPane, studentClassListView, studentClassButtonPane);
        studentClassPane.setSpacing(10);

        Text studentResultText = new Text("Student Result");
        HBox studentResultTextPane = new HBox(studentResultText);
        studentResultTextPane.setAlignment(Pos.CENTER);
        TableView<Result> studentResultTableView = new TableView<>();
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
        Button editResultButton = new Button("Edit Result");
        editResultButton.setOnAction(e -> {
            //TODO
        });
        Button regSuppExamButton = new Button("Register Supp Exam");
        regSuppExamButton.setOnAction(e -> {
            //TODO
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
        Button editAttendanceButton = new Button("Edit Attendance");
        editAttendanceButton.setOnAction(e -> {
            //TODO
        });
        Button removeAttendanceButton = new Button("Remove Attendance");
        removeAttendanceButton.setOnAction(e -> {
            //TODO
        });
        HBox studentAttendanceButtonPane = new HBox(editAttendanceButton, removeAttendanceButton);
        studentAttendanceButtonPane.setSpacing(10);
        studentAttendanceButtonPane.setAlignment(Pos.CENTER);
        VBox studentAttendancePane = new VBox(studentAttendanceTextPane, studentAttendanceTableView, studentAttendanceButtonPane);
        studentAttendancePane.setSpacing(10);

        HBox studentDataPane = new HBox(studentClassPane, studentResultPane, studentAttendancePane);
        studentDataPane.setAlignment(Pos.CENTER);
        studentDataPane.setSpacing(25);

        VBox studentInfoPane = new VBox(studentInfoTextArea, studentDataPane);
        studentInfoPane.setPadding(new Insets(20));
        studentInfoPane.setSpacing(25);
        studentInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(studentInfoPane, Priority.ALWAYS);

        HBox studentPane = new HBox(studentSearchPane, studentInfoPane);
        connectionHandler.student.updated.addListener(e -> {
            if (connectionHandler.student.updated.get()) {
                Platform.runLater(() -> studentClassListView.getItems().clear());
                if (connectionHandler.student.getStudent() != null) {
                    Platform.runLater(() -> {
                        studentInfoTextArea.setText(connectionHandler.student.getStudent().getStudentInformation());
                        studentClassListView.setItems(FXCollections.observableArrayList(connectionHandler.student.getStudent().getClassResultAttendances()));
                    });
                } else {
                    studentInfoTextArea.setText("Please select student");
                }
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
            //TODO
        });

        Circle lecturerPicture = new Circle(30);
        lecturerPicture.setStroke(Color.BLACK);
        lecturerPicture.setStrokeWidth(2);
        lecturerPicture.setFill(new ImagePattern(new Image(getClass().getClassLoader().getResourceAsStream("DefaultProfile.jpg"))));
        TextArea lecturerInfoTextArea = new TextArea("");
        lecturerInfoTextArea.setEditable(false);
        HBox lecturerTopPane = new HBox(lecturerPicture, lecturerInfoTextArea);
        lecturerTopPane.setSpacing(15);

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

        VBox lecturerInfoPane = new VBox(lecturerTopPane, lecturerClassPane);
        lecturerInfoPane.setPadding(new Insets(20));
        lecturerInfoPane.setSpacing(25);
        lecturerInfoPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(lecturerInfoPane, Priority.ALWAYS);
        HBox lecturerPane = new HBox(lecturerSearchPane, lecturerInfoPane);

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
        Button addNoticeButton = new Button("Add Notice");
        addNoticeButton.setOnAction(e -> {
            //TODO
        });
        Button editNoticeButton = new Button("Edit Notice");
        editNoticeButton.setOnAction(e -> {
            //TODO
        });
        Button removeNoticeButton = new Button("Edit Notice");
        removeNoticeButton.setOnAction(e -> {
            //TODO
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
        notificationIDTableColumn.setResizable(false);
        notificationIDTableColumn.setMaxWidth(50);
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
        Button addNotificationButton = new Button("Add Notification");
        addNotificationButton.setOnAction(e -> {
            //TODO
        });
        Button editNotificationButton = new Button("Edit Notification");
        editNotificationButton.setOnAction(e -> {
            //TODO
        });
        Button removeNotificationButton = new Button("Remove Notification");
        removeNotificationButton.setOnAction(e -> {
            //TODO
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
        Button addDateButton = new Button("Add Date");
        addDateButton.setOnAction(e -> {
            //TODO
        });
        Button editDateButton = new Button("Edit Date");
        editDateButton.setOnAction(e -> {
            //TODO
        });
        Button removeDateButton = new Button("Remove Date");
        removeDateButton.setOnAction(e -> {
            //TODO
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
        Tab classTab = new Tab("Class", vBox);
        Tab contactTab = new Tab("Contact Details", vBox);
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
