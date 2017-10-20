package net.ddns.swooosh.campusliveadmin.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.admin.*;
import models.all.*;

import javax.net.ssl.SSLSocketFactory;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler {

    private static final int PORT = 25760;
    private static final String LOCAL_ADDRESS = "127.0.0.1";
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private List<String> inputQueue = new ArrayList<>();
    ObservableList<StudentClass> classes = FXCollections.observableArrayList();
    ObservableList<Admin> admins = FXCollections.observableArrayList();
    ObservableList<AdminSearch> studentSearches = FXCollections.observableArrayList();
    StudentObservable student = new StudentObservable();
    ObservableList<AdminSearch> lecturerSearches = FXCollections.observableArrayList();
    LecturerObservable lecturer = new LecturerObservable();
    ObservableList<AdminSearch> classSearches = FXCollections.observableArrayList();
    ClassObservable studentClass = new ClassObservable();
    ObservableList<AdminSearch> contactSearches = FXCollections.observableArrayList();
    ContactDetailsObservable contactDetails = new ContactDetailsObservable();
    ObservableList<Notice> notices = FXCollections.observableArrayList();
    ObservableList<Notification> notifications = FXCollections.observableArrayList();
    ObservableList<ImportantDate> importantDates = FXCollections.observableArrayList();
    AdminLogObservable adminLog = new AdminLogObservable();

    ConnectionHandler() {
        connect();
        new InputProcessor().start();
        studentSearches.addAll(new AdminSearch("student", "Stephan Malan", "DV2015-0073"),
                new AdminSearch("student", "Ronald Muller", "DV2015-0103"),
                new AdminSearch("student", "Jaco Lintvelt", "DV2015-0135"));

    }

    private void connect() {
        try {
            System.out.println("Trying to connect to server...");
            System.setProperty("javax.net.ssl.trustStore", Display.APPLICATION_FOLDER + "/campuslive.store");
            socket = SSLSocketFactory.getDefault().createSocket(LOCAL_ADDRESS, PORT);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Socket is connected");
        } catch (Exception ex) {
            ex.printStackTrace();
            UserNotification.showErrorMessage("Connection error", "Could not connect to server!\nPlease check network connectivity and try again!");
            System.exit(0);
        }
    }

    public Boolean login(String username, String password) {
        sendData("aa:" + username + ":" + password);
        return getStringReply("aa:");
    }

    void requestStudent(String studentNumber) {
        sendData("asd:" + studentNumber);
    }

    void unregisterClass(String studentNumber, int classID) {
        sendData("uc:" + studentNumber + ":" + classID);
    }

    void registerClass(String studentNumber, int classID) {
        sendData("rsc:" + studentNumber + ":" + classID);
    }

    private void sendData(String data) {
        try {
            objectOutputStream.writeUTF(data);
            objectOutputStream.flush();
            System.out.println("Sent data: " + data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Object getReply() {
        try {
            Object input;
            while ((input = objectInputStream.readObject()) == null) ;
            return input;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    private Boolean getStringReply(String startsWith) {
        Boolean result;
        String objectToRemove;
        ReturnResult:
        while (true) {
            for (String in : inputQueue) {
                if (in.startsWith(startsWith)) {
                    objectToRemove = in;
                    result = in.charAt(startsWith.length()) == 'y';
                    break ReturnResult;
                }
            }
        }
        inputQueue.remove(objectToRemove);
        return result;
    }

    ObservableList getAllClasses() {
        classes.clear();
        sendData("gac:");
        while (classes.size() < 1) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    void requestLogFile() {
        sendData("asl:");
    }

    private class InputProcessor extends Thread {
        public void run() {
            while (true) {
                Object input;
                if ((input = getReply()) != null) {
                    System.out.println(input);
                    if (input instanceof Student) {
                        student.setStudent((Student) input);
                    } else if (input instanceof Lecturer) {
                        lecturer.setLecturer((Lecturer) input);
                    } else if (input instanceof StudentClass) {
                        studentClass.setStudentClass((StudentClass) input);
                    } else if (input instanceof ContactDetails) {
                        contactDetails.setContactDetails((ContactDetails) input);
                    } else if (input instanceof AdminLog) {
                        adminLog.setAdminLog((AdminLog) input);
                    } else if (input instanceof List<?>) {
                        List list = (List) input;
                        if (!list.isEmpty() && list.get(0) instanceof AdminSearch) {
                            if (((AdminSearch) list.get(0)).getType().equals("Student")) {
                                studentSearches.clear();
                                if (!((AdminSearch) list.get(0)).getPrimaryText().equals("")) {
                                    studentSearches.addAll(list);
                                }
                            } else if (((AdminSearch) list.get(0)).getType().equals("Lecturer")) {
                                lecturerSearches.clear();
                                if (!((AdminSearch) list.get(0)).getPrimaryText().equals("")) {
                                    lecturerSearches.addAll(list);
                                }
                            } else if (((AdminSearch) list.get(0)).getType().equals("Class")) {
                                classSearches.clear();
                                if (!((AdminSearch) list.get(0)).getPrimaryText().equals("")) {
                                    classSearches.addAll(list);
                                }
                            } else if (((AdminSearch) list.get(0)).getType().equals("Contact")) {
                                contactSearches.clear();
                                if (!((AdminSearch) list.get(0)).getPrimaryText().equals("")) {
                                    contactSearches.addAll(list);
                                }
                            }
                        } else if (!list.isEmpty() && list.get(0) instanceof Admin) {
                            admins.clear();
                            admins.addAll(list);
                        } else if (!list.isEmpty() && list.get(0) instanceof Notice) {
                            notices.clear();
                            notices.addAll(list);
                        } else if (!list.isEmpty() && list.get(0) instanceof Notification) {
                            notifications.clear();
                            notifications.addAll(list);
                        } else if (!list.isEmpty() && list.get(0) instanceof ImportantDate) {
                            importantDates.clear();
                            importantDates.addAll(list);
                        } else if (!list.isEmpty() && list.get(0) instanceof StudentClass) {
                            classes.clear();
                            classes.addAll(list);
                        }
                    } else if (input instanceof String) {
                        inputQueue.add((String) input);
                    }
                }
            }
        }
    }

}
