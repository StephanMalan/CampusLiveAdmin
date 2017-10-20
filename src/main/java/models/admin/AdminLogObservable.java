package models.admin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AdminLogObservable {

    AdminLog adminLog;
    public BooleanProperty updated = new SimpleBooleanProperty(false);

    public void setAdminLog(AdminLog adminLog) {
        this.adminLog = adminLog;
        updated.set(true);
    }

    public AdminLog getAdminLog() {
        return adminLog;
    }
}
