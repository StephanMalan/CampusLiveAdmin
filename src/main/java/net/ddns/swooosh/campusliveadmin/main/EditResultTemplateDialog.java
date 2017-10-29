package net.ddns.swooosh.campusliveadmin.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.all.ResultTemplate;

import java.util.ArrayList;
import java.util.List;

public class EditResultTemplateDialog extends CustomDialogSkin {

    public EditResultTemplateDialog(Window parent, ConnectionHandler connectionHandler, List<ResultTemplate> resultTemplates) {
        initOwner(parent);
        Text headingText = new Text("Edit Result Template");

        VBox innerPane = new VBox(headingText);
        ObservableList<ResultPane> panes = FXCollections.observableArrayList();
        for (ResultTemplate rt : resultTemplates) {
            ResultPane resultPane = new ResultPane(rt);
            panes.add(resultPane);
            innerPane.getChildren().add(resultPane);
        }

        Button actionButton = new Button("Edit");
        actionButton.setOnAction(e -> {
            List<ResultTemplate> newTemplates = new ArrayList<>();
            Boolean valid = true;
            Boolean changed = false;
            int dpWeight = 0;
            int finalWeight = 0;
            int examWeight = 0;
            int suppWeight = 0;
            for (ResultPane rp : panes) {
                if (!rp.isValid()) {
                    valid = false;
                    break;
                }
                if (rp.changed()) {
                    changed = true;
                }
                newTemplates.add(rp.getTemplate());
                if (!rp.getResultName().equals("Supplementary Exam")) {
                    dpWeight += rp.getDPWeight();
                    finalWeight += rp.getFinalWeight();
                }
                if (rp.getResultName().equals("Supplementary Exam")) {
                    suppWeight = rp.getFinalWeight();
                } else if (rp.getResultName().equals("Initial Exam")) {
                    examWeight = rp.getFinalWeight();
                }
            }
            if (valid) {
                if (dpWeight == 100 && finalWeight == 100) {
                    if (suppWeight == examWeight) {
                        if (changed) {
                            connectionHandler.sendResultTemplates(newTemplates);
                        }
                        closeAnimation();
                    } else {
                        UserNotification.showErrorMessage("Edit Result Template", "Initial Exam and Supplementary Exam weight must be the same");
                    }
                } else {
                    UserNotification.showErrorMessage("Edit Result Template", "Invalid total DP Weight and Final Weight\nTotal should be 100");
                }
            }

        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        innerPane.getChildren().add(buttonPane);
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

    public class ResultPane extends HBox {

        private ResultTemplate template;
        private Label label;
        private NumberTextField resultMaxTextField;
        private NumberTextField dpWeightTextField;
        private NumberTextField finalWeightTextField;

        public ResultPane(ResultTemplate template) {
            this.template = template;
            label = new Label(template.getResultName());
            Pane filler = new Pane();
            HBox.setHgrow(filler, Priority.ALWAYS);
            resultMaxTextField = new NumberTextField("Result Max");
            resultMaxTextField.setMaxWidth(75);
            resultMaxTextField.setText(template.getResultMax() + "");
            dpWeightTextField = new NumberTextField("DP Weight");
            dpWeightTextField.setMaxWidth(75);
            dpWeightTextField.setText(template.getDpWeight() + "");
            if (template.getResultName().equals("Initial Exam") || template.getResultName().equals("Supplementary Exam")) {
                dpWeightTextField.setEditable(false);
            }
            finalWeightTextField = new NumberTextField("Final Weight");
            finalWeightTextField.setMaxWidth(75);
            finalWeightTextField.setText(template.getFinalWeight() + "");
            setSpacing(15);
            getChildren().addAll(label, filler, resultMaxTextField, dpWeightTextField, finalWeightTextField);
        }

        Boolean isValid() {
            if (!resultMaxTextField.getText().isEmpty()) {
                if (!dpWeightTextField.getText().isEmpty() && Integer.parseInt(dpWeightTextField.getText()) <= 100) {
                    if (!finalWeightTextField.getText().isEmpty() && Integer.parseInt(finalWeightTextField.getText()) <= 100) {
                        return true;
                    } else {
                        UserNotification.showErrorMessage("Add Result Template", label + ") Invalid Final Weight");
                    }
                } else {
                    UserNotification.showErrorMessage("Add Result Template", label + ") Invalid DP Weight");
                }
            } else {
                UserNotification.showErrorMessage("Add Result Template", label + ") Invalid Result Max");
            }
            return false;
        }

        Boolean changed() {
            return template.getResultMax() != getResultMax() || template.getDpWeight() != getDPWeight() || template.getFinalWeight() != getFinalWeight();
        }

        String getResultName() {
            return template.getResultName();
        }

        private int getResultMax() {
            if (resultMaxTextField.getText().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(resultMaxTextField.getText());
        }

        int getDPWeight() {
            if (dpWeightTextField.getText().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(dpWeightTextField.getText());
        }

        int getFinalWeight() {
            if (finalWeightTextField.getText().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(finalWeightTextField.getText());
        }

        ResultTemplate getTemplate() {
            return new ResultTemplate(template.getId(), template.getClassID(), getResultMax(), getDPWeight(), getFinalWeight(), template.getResultName());
        }

    }

}
