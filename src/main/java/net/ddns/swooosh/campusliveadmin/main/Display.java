package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Display extends Application{

    public void start(Stage stage) throws Exception {
        //Setup stage
        stage.setMaximized(true);
        stage.setTitle("CampusLiveAdmin");

        //Test
        VBox vBox = new VBox(new Label("Test!"));

        //Setup tab pane
        Tab adminTab = new Tab("Admin", vBox);
        Tab studentTab = new Tab("Student", vBox);
        Tab lecturerTab = new Tab("Lecturer", vBox);
        Tab classTab = new Tab("Class", vBox);
        Tab contactTab = new Tab("Contact Details", vBox);
        Tab noticeTab = new Tab("Notices", vBox);
        Tab notificationTab = new Tab("Notifications", vBox);
        Tab datesTab = new Tab("Important Dates", vBox);
        Tab logTab = new Tab("Server Log", vBox);
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
