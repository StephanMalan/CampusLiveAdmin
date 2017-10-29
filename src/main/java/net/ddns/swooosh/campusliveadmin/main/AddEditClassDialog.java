package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.admin.AdminSearch;
import models.all.ClassLecturer;
import models.all.ResultTemplate;
import models.all.StudentClass;

import java.util.ArrayList;
import java.util.List;

public class AddEditClassDialog extends CustomDialogSkin {

    public AddEditClassDialog(Window parent, ConnectionHandler connectionHandler, StudentClass studentClass) {
        initOwner(parent);
        ObservableList<ResultTemplate> templates = FXCollections.observableArrayList();
        String heading;
        if (studentClass != null) {
            heading = "Edit Class";
            templates.addAll(studentClass.getResultTemplates());
        } else {
            heading = "Add Class";
        }
        Text headingText = new Text(heading);
        TextField moduleNameTextField = new TextField();
        moduleNameTextField.setPromptText("Module Name");
        moduleNameTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        TextField moduleNumberTextField = new TextField();
        moduleNumberTextField.setPromptText("Module Number");
        moduleNumberTextField.setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -45%);");
        ComboBox<AdminSearch> lecturerComboBox = new ComboBox<>(connectionHandler.lecturerSearches);
        lecturerComboBox.setCellFactory(e -> new ListCell<AdminSearch>() {
            @Override
            protected void updateItem(AdminSearch item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getPrimaryText() + ": " + item.getSecondaryText());
                }
            }
        });
        lecturerComboBox.setButtonCell(new ListCell<AdminSearch>() {
            @Override
            protected void updateItem(AdminSearch item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getPrimaryText() + ": " + item.getSecondaryText());
                }
            }
        });
        lecturerComboBox.setPromptText("Lecturer");
        TableView<ResultTemplate> classResultTemplateTableView = new TableView<>(templates);
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
        Button removeResultTemplate = new Button("Remove");
        removeResultTemplate.setOnAction(e -> {
            if (!classResultTemplateTableView.getSelectionModel().isEmpty()) {
                if (!classResultTemplateTableView.getSelectionModel().getSelectedItem().getResultName().equals("Totals")) {
                    templates.remove(classResultTemplateTableView.getSelectionModel().getSelectedItem());
                }
            }
        });
        classResultTemplateTableView.getItems().addListener((ListChangeListener<? super ResultTemplate>) e -> {
            Boolean noTotals = true;
            for (ResultTemplate rt : classResultTemplateTableView.getItems()) {
                if (rt.getResultName().equals("Totals")) {
                    noTotals = false;
                    Platform.runLater(() -> classResultTemplateTableView.getItems().remove(rt));
                    break;
                }
            }
            if (noTotals && !classResultTemplateTableView.getItems().get(classResultTemplateTableView.getItems().size() - 1).getResultName().equals("Totals")) {
                int dpWeight = 0;
                int finalWeight = 0;
                for (ResultTemplate rt : templates) {
                    if (!rt.getResultName().equals("Supplementary Exam")) {
                        dpWeight += rt.getDpWeight();
                        finalWeight += rt.getFinalWeight();
                    }
                }
                classResultTemplateTableView.getItems().add(new ResultTemplate(0, 0, 0, dpWeight, finalWeight, "Totals"));
            }
        });

        ComboBox<String> resultNameComboBox = new ComboBox<>(FXCollections.observableArrayList("Semester Test", "Assignment", "Project", "Cont Assessment", "Initial Exam", "Supplementary Exam"));
        resultNameComboBox.setPromptText("Result Name");
        NumberTextField resultMaxTextField = new NumberTextField("Result Max");
        NumberTextField dpWeightTextField = new NumberTextField("DP Weight");
        NumberTextField finalWeightTextField = new NumberTextField("Final Weight");
        resultNameComboBox.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (!resultNameComboBox.getSelectionModel().isEmpty()) {
                if (resultNameComboBox.getSelectionModel().getSelectedItem().equals("Initial Exam") || resultNameComboBox.getSelectionModel().getSelectedItem().equals("Supplementary Exam")) {
                    dpWeightTextField.setText("0");
                    dpWeightTextField.setEditable(false);
                } else {
                    dpWeightTextField.setEditable(true);
                }
            }
        });
        Button addResultButton = new Button("Add");
        addResultButton.setOnAction(e -> {
            if (!resultNameComboBox.getSelectionModel().isEmpty()) {
                if (!resultMaxTextField.getText().isEmpty()) {
                    if (!dpWeightTextField.getText().isEmpty() && Integer.parseInt(dpWeightTextField.getText()) <= 100) {
                        if (!finalWeightTextField.getText().isEmpty() && Integer.parseInt(finalWeightTextField.getText()) <= 100) {
                            templates.add(new ResultTemplate(0, 0, Integer.parseInt(resultMaxTextField.getText()), Integer.parseInt(dpWeightTextField.getText()), Integer.parseInt(finalWeightTextField.getText()), resultNameComboBox.getSelectionModel().getSelectedItem()));
                            resultNameComboBox.getSelectionModel().clearSelection();
                            resultMaxTextField.setText("");
                            dpWeightTextField.setText("");
                            finalWeightTextField.setText("");
                        } else {
                            UserNotification.showErrorMessage("Add Result Template", "Invalid Final Weight");
                        }
                    } else {
                        UserNotification.showErrorMessage("Add Result Template", "Invalid DP Weight");
                    }
                } else {
                    UserNotification.showErrorMessage("Add Result Template", "Invalid Result Max");
                }
            } else {
                UserNotification.showErrorMessage("Add Result Template", "Please select Result Name");
            }
        });
        HBox addResultPane = new HBox(resultNameComboBox, resultMaxTextField, dpWeightTextField, finalWeightTextField, addResultButton);
        addResultPane.setSpacing(15);

        Button actionButton = new Button();
        actionButton.setOnAction((ActionEvent e) -> {
            if (!moduleNameTextField.getText().isEmpty() && moduleNameTextField.getText().matches("[a-zA-Z ]*")) {
                if (!moduleNumberTextField.getText().isEmpty() && moduleNumberTextField.getText().matches("[A-Z0-9 ]*")) {
                    if (!lecturerComboBox.getSelectionModel().isEmpty()) {
                        if (!classResultTemplateTableView.getItems().isEmpty()) {
                            if (classResultTemplateTableView.getItems().get(classResultTemplateTableView.getItems().size() - 1).getDpWeight() == 100 && classResultTemplateTableView.getItems().get(classResultTemplateTableView.getItems().size() - 1).getFinalWeight() == 100) {
                                if (bothExams(templates)) {
                                    if (studentClass != null) {
                                        if (!studentClass.getModuleNumber().equals(moduleNumberTextField.getText()) || !studentClass.getModuleName().equals(moduleNameTextField.getText()) || !studentClass.getClassLecturer().getLecturerID().equals(lecturerComboBox.getSelectionModel().getSelectedItem().getSecondaryText())) {
                                            connectionHandler.sendStudentClass(new StudentClass(studentClass.getClassID(), moduleNameTextField.getText(), moduleNumberTextField.getText(), new ClassLecturer(lecturerComboBox.getSelectionModel().getSelectedItem().getSecondaryText(), "", "", "", "", null), null, null, null));
                                        }
                                    } else {
                                        connectionHandler.sendStudentClass(new StudentClass(-1, moduleNameTextField.getText(), moduleNumberTextField.getText(), new ClassLecturer(lecturerComboBox.getSelectionModel().getSelectedItem().getSecondaryText(), "", "", "", "", null), null, null, getResultTemplates(templates)));
                                    }
                                    closeAnimation();
                                } else {
                                    UserNotification.showErrorMessage(heading, "Initial Exam must be accompanied by Supp Exam and visa versa\nInitial and Supp Exam weight must be identical");
                                }
                            } else {
                                UserNotification.showErrorMessage(heading, "Invalid total DP Weight and Final Weight\nTotal should be 100");
                            }
                        } else {
                            UserNotification.showErrorMessage(heading, "Please add Result Templates");
                        }
                    } else {
                        UserNotification.showErrorMessage(heading, "Please select Lecturer");
                    }
                } else {
                    UserNotification.showErrorMessage(heading, "Invalid Module Number");
                }
            } else {
                UserNotification.showErrorMessage(heading, "Invalid Module Name");
            }

        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        if (studentClass != null) {
            actionButton.setText("Edit");
            moduleNameTextField.setText(studentClass.getModuleName());
            moduleNumberTextField.setText(studentClass.getModuleNumber());
            for (AdminSearch adminSearch : lecturerComboBox.getItems()) {
                if (adminSearch.getSecondaryText().equals(studentClass.getClassLecturer().getLecturerID())) {
                    lecturerComboBox.getSelectionModel().select(adminSearch);
                    break;
                }
            }
        } else {
            actionButton.setText("Add");
        }
        VBox innerPane = new VBox();
        if (studentClass == null) {
            innerPane.getChildren().addAll(headingText, moduleNameTextField, moduleNumberTextField, lecturerComboBox, classResultTemplateTableView, removeResultTemplate, addResultPane, buttonPane);
        } else {
            innerPane.getChildren().addAll(headingText, moduleNameTextField, moduleNumberTextField, lecturerComboBox, buttonPane);
        }
        innerPane.setPadding(new Insets(20, 50, 20, 50));
        innerPane.setSpacing(20);
        innerPane.setMinWidth(800);
        innerPane.setMaxWidth(800);
        innerPane.setAlignment(Pos.CENTER);
        innerPane.setStyle("-fx-background-color: #ffffff;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;");
        VBox contentPane = new VBox(innerPane);
        contentPane.setAlignment(Pos.CENTER);
        setWidth(800);
        getDialogPane().setContent(contentPane);
    }

    private Boolean bothExams(List<ResultTemplate> resultTemplates) {
        int examWeight = -1;
        int suppWeight = -1;
        for (ResultTemplate rt : resultTemplates) {
            if (rt.getResultName().equals("Initial Exam")) {
                examWeight = rt.getFinalWeight();
            } else if (rt.getResultName().equals("Supplementary Exam")) {
                suppWeight = rt.getFinalWeight();
            }
        }
        return examWeight == suppWeight;
    }

    private List<ResultTemplate> getResultTemplates(ObservableList<ResultTemplate> resultTemplates) {
        List<ResultTemplate> out = new ArrayList<>();
        for (ResultTemplate rt : resultTemplates) {
            if (!rt.getResultName().equals("Totals")) {
                out.add(rt);
            }
        }
        return out;
    }

}
