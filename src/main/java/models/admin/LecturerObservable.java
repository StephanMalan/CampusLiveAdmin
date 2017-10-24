package models.admin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import models.all.Lecturer;

public class LecturerObservable {

    Lecturer lecturer;
    public BooleanProperty updated = new SimpleBooleanProperty(false);

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
        updated.set(true);
    }

    public Lecturer getLecturer() {
        return lecturer;
    }
}
