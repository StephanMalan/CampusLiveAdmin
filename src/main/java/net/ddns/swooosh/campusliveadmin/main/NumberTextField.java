package net.ddns.swooosh.campusliveadmin.main;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField {

    public NumberTextField() {
        super();
        setup();
    }

    public NumberTextField(String promptText) {
        super();
        setPromptText(promptText);
        setStyle("-fx-prompt-text-fill: derive(-fx-control-inner-background, -30%);");
        setup();
    }

    private void setup() {
        textProperty().addListener((obs, oldV, newV) -> {
            String value = digitsOnly(newV);
            setText(value);
            positionCaret(value.length());
        });
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
