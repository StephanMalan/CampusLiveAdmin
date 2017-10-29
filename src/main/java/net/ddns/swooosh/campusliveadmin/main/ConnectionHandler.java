package net.ddns.swooosh.campusliveadmin.main;

import javafx.application.Platform;
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
    private static final String LOCAL_ADDRESS = "127.0.0.1"; //TODO
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private volatile List<String> inputQueue = new ArrayList<>();
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

    void requestLecturer(String lecturerNumber) {
        sendData("ald:" + lecturerNumber);
    }

    void requestClass(String classID) {
        sendData("acd:" + classID);
    }

    void requestContact(String name, String position) {
        sendData("aci:" + name + ":" + position);
    }

    void unregisterClass(String studentNumber, int classID) {
        sendData("uc:" + studentNumber + ":" + classID);
    }

    void registerClass(String studentNumber, int classID) {
        sendData("rsc:" + studentNumber + ":" + classID);
    }

    void resetAdminPassword(String username, String email) {
        sendData("rap:" + username + ":" + email);
    }

    void resetStudentPassword() {
        sendData("rsp:" + student.getStudent().getStudentNumber() + ":" + student.getStudent().getEmail());
    }

    void resetLecturerPassword() {
        sendData("rlp:" + lecturer.getLecturer().getLecturerNumber() + ":" + lecturer.getLecturer().getEmail());
    }

    void sendAdmin(Admin admin) {
        sendData(admin);
    }

    void removeStudent() {
        sendData("rs:" + student.getStudent().getStudentNumber());
    }

    void removeLecturer() {
        sendData("rl:" + lecturer.getLecturer().getLecturerNumber());
    }

    void removeAttendance(int attendanceID) {
        sendData("ra:" + attendanceID);
    }

    void removeClass() {
        sendData("rc:" + studentClass.getStudentClass().getClassID());
    }

    void removeClassTime(int classTimeID) {
        sendData("rct:" + classTimeID);
    }

    void removeContact() {
        sendData("rd:" + contactDetails.getContactDetails().getId());
    }

    void removeNotice(int noticeID) {
        sendData("rn:" + noticeID);
    }

    void removeNotification(int notificationID) {
        sendData("rf:" + notificationID);
    }

    void removeDate(int dateID) {
        sendData("ri:" + dateID);
    }

    void removeAdmin(String username) {
        sendData("rm" + username);
    }

    void sendStudent(Student student) {
        sendData(student);
    }

    void sendAttendance(Attendance attendance) {
        sendData(attendance);
    }

    void sendLecturer(Lecturer lecturer) {
        sendData(lecturer);
    }

    void sendClassTime(ClassTime classTime) {
        sendData(classTime);
    }

    void sendNotice(Notice notice) {
        sendData(notice);
    }

    void sendNotification(Notification notification) {
        sendData(notification);
    }

    void updateResult(Result result) {
        sendData(result);
    }

    void sendDate(ImportantDate importantDate) {
        sendData(importantDate);
    }

    void sendContact(ContactDetails contactDetails) {
        sendData(contactDetails);
    }

    void sendStudentClass(StudentClass studentClass) {
        sendData(studentClass);
    }

    void sendResultTemplates(List<ResultTemplate> resultTemplate) {
        sendData(resultTemplate);
    }

    void regSuppExam(String studentNumber, int classID) {
        sendData("rse:" + studentNumber + ":" + classID);
    }

    public Boolean changeDefaultPassword(String newPassword) {
        sendData("cdp:" + newPassword);
        return getStringReply("cdp:");
    }

    public boolean isDefaultPassword() {
        sendData("idp:");
        return getStringReply("idp:");
    }

    private void sendData(Object data) {
        try {
            objectOutputStream.writeObject(data);
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

    private synchronized Boolean getStringReply(String startsWith) {
        System.out.println(startsWith);
        Boolean result;
        String objectToRemove;
        ReturnResult:
        while (true) {
            for (int i = 0; i < inputQueue.size(); i++) {
                try {
                    if (inputQueue.get(i).startsWith(startsWith)) {
                        objectToRemove = inputQueue.get(i);
                        result = inputQueue.get(i).charAt(startsWith.length()) == 'y';
                        break ReturnResult;
                    }
                } catch (Exception ex) {
                }
            }
        }
        //TODO investigate running multiple times
        inputQueue.remove(objectToRemove);
        System.out.println("Got reply> " + objectToRemove);
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
        //TODO will fuck out if classes.length == 0
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
                        System.out.println("lel");
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
                                Platform.runLater(() -> {
                                    lecturerSearches.clear();
                                    if (!((AdminSearch) list.get(0)).getPrimaryText().equals("")) {
                                        lecturerSearches.addAll(list);
                                    }
                                });
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
                            System.out.println(1);
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
