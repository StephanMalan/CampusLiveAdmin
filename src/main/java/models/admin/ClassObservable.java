package models.admin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import models.all.StudentClass;

public class ClassObservable {

    StudentClass studentClass;
    BooleanProperty updated = new SimpleBooleanProperty(false);

    public void setStudentClass(StudentClass studentClass) {
        this.studentClass = studentClass;
        updated.set(true);
    }

}
