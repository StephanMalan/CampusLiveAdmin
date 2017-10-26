package net.ddns.swooosh.campusliveadmin.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import models.all.Result;

public class EditResultDialog extends CustomDialogSkin {

    public EditResultDialog(Window parent, ConnectionHandler connectionHandler, Result result) {
        initOwner(parent);
        Text headingText = new Text("Edit Result");
        NumberTextField resultTextField = new NumberTextField("Result");
        CheckBox noResultCheckBox = new CheckBox("No Result");
        StringProperty prevResult = new SimpleStringProperty("");
        noResultCheckBox.selectedProperty().addListener(e -> {
            if (noResultCheckBox.isSelected()) {
                prevResult.set(resultTextField.getText());
                resultTextField.setText("");
                resultTextField.setPromptText("No Result");
                resultTextField.setEditable(false);
            } else {
                resultTextField.setText(prevResult.get());
                resultTextField.setPromptText("Result");
                resultTextField.setEditable(true);
            }
        });
        if (result.getResult() == -1D) {
            noResultCheckBox.setSelected(true);
        } else {
            resultTextField.setText(result.getResult() + "");
        }
        Button actionButton = new Button("Edit");
        actionButton.setOnAction(e -> {
            if (noResultCheckBox.isSelected()) {
                if (result.getResult() != -1D) {
                    result.setResult(-1);
                    connectionHandler.updateResult(result);
                }
                closeAnimation();
            } else {
                if (!resultTextField.getText().isEmpty()) {
                    int resultInt = Integer.parseInt(resultTextField.getText());
                    if (resultInt >= 0 && resultInt <= result.getResultMax()) {
                        if (result.getResult() != resultInt) {
                            result.setResult(resultInt);
                            connectionHandler.updateResult(result);
                        }
                        closeAnimation();
                    } else {
                        UserNotification.showErrorMessage("Edit Result", "Result has to be between 0 and " + String.format("%.0f", result.getResultMax()));
                    }
                } else {
                    UserNotification.showErrorMessage("Edit Result", "Result can't be empty");
                }
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> closeAnimation());
        HBox buttonPane = new HBox(actionButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setSpacing(15);
        VBox innerPane = new VBox(headingText, resultTextField, noResultCheckBox, buttonPane);
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
