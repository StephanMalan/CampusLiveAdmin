package models.admin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import models.all.ContactDetails;

public class ContactDetailsObservable {

    ContactDetails contactDetails;
    BooleanProperty updated = new SimpleBooleanProperty(false);

    public void setContactDetails(ContactDetails contactDetails) {
        this.contactDetails = contactDetails;
        updated.set(true);
    }

}
