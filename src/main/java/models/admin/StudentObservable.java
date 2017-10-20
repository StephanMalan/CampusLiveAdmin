package models.admin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import models.all.Student;

public class StudentObservable {

    Student student;
    public BooleanProperty updated = new SimpleBooleanProperty(false);

    public void setStudent(Student student) {
        this.student = student;
        updated.set(true);
    }

    public Student getStudent() {
        return student;
    }
}
