package models.all;

import java.io.Serializable;

public class ImportantDate implements Serializable {

    private String date;
    private String description;

    public ImportantDate(String date, String description) {
        this.date = date;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

}
