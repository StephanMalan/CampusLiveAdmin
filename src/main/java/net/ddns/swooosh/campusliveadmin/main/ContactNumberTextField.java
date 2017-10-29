package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Platform;
import javafx.scene.control.TextField;

public class ContactNumberTextField extends TextField {

    public ContactNumberTextField() {
        super();
        setup();
    }

    public ContactNumberTextField(String promptText) {
        super();
        setPromptText(promptText);
        setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -30%);");
        setup();
    }

    private void setup() {
        textProperty().addListener((obs, oldV, newV) -> {
            String values = digitsOnly(newV);
            if (values.length() > 10) {
                values = values.substring(0, 10);
            }
            final String temp = values;
            if (values.length() > 6) {
                Platform.runLater(() -> {
                    setText(temp.substring(0, 3) + " " + temp.substring(3, 6) + " " + temp.substring(6));
                    positionCaret(temp.length() + 2);
                });
            } else if (values.length() > 3) {
                Platform.runLater(() -> {
                    setText(temp.substring(0, 3) + " " + temp.substring(3));
                    positionCaret(temp.length() + 1);
                });
            } else {
                Platform.runLater(() -> {
                    setText(temp);
                    positionCaret(temp.length());
                });
            }
        });
    }

    public String getNumber() {
        if (getText().length() > 12) return getText().substring(0, 12);
        return getText();
    }

    private String digitsOnly(String in) {
        String out = "";
        if (in != null) {
            for (Character c : in.toCharArray()) {
                if (Character.isDigit(c)) {
                    out += c;
                }
            }
        }
        return out;
    }
}
