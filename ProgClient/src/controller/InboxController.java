package controller;

import Interface.NewEmailData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.Mailbox;
import common.MailboxStorage;
import common.NetworkPacket;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import network.ServerConnection;
import common.Email;
import utils.AlertUtil;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class InboxController implements Initializable {
    @FXML
    private Label lblConnState;

    @FXML
    private Label lblMe;

    @FXML
    private Button btnNewEmail;
    @FXML
    private Button btnReplyEmail;
    @FXML
    private Button btnReplyAllEmail;
    @FXML
    private Button btnForwardEmail;
    @FXML
    private Button btnDeleteEmail;

    @FXML
    private Label lblFrom;
    @FXML
    private Label lblTo;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblSubject;
    @FXML
    private Label lblBody;
    @FXML
    private Label lblNotification;

    @FXML
    ListView<Email> emailListView = new ListView<>();

    private ObservableList<Email> observableEmails = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        emailListView.setItems(observableEmails);
        lblMe.setText(Session.getInstance().getEmail());
        try {
            updateEmailUi();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        launchPingThread();
        launchNewEmailThread();
        emailListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);

                if (empty || email == null) {
                    setText(null);
                } else {
                    setText(email.getFrom() + " - " + email.getSubject());
                }
            }
        });
        emailListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showEmail(newSelection);
                    }
                });
    }

    private void launchPingThread() {
        new Thread(()-> {
            while (running) {
                ServerConnection serverConnection = ServerConnection.getInstance();
                try {
                    Thread.sleep(400);
                    NetworkPacket networkPacket = serverConnection.ping();
                    if (networkPacket.isSuccess()) {
                        Platform.runLater(()->{
                            lblConnState.setTextFill(Color.GREEN);
                            lblConnState.setText("online");});
                            setButtonsEnabled(true);
                    }
                } catch (IOException e) {
                    Platform.runLater(()->{
                        lblConnState.setTextFill(Color.RED);
                        lblConnState.setText("offline");});
                        setButtonsEnabled(false);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void showEmail(Email email){
        if (email == null) {
            lblFrom.setText("");
            lblTo.setText("");
            lblDate.setText("");
            lblSubject.setText("");
            lblBody.setText("");
        }
        else {
            lblFrom.setText("From: " + email.getFrom());
            lblTo.setText("To: " + String.join(", ", email.getTo()));
            lblDate.setText("Date: " + email.getDate());
            lblSubject.setText("Subject: " + email.getSubject());
            lblBody.setText("Body: " + email.getBody());
        }
    }

    private volatile boolean running = true;

    private void launchNewEmailThread() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    ServerConnection serverConnection = ServerConnection.getInstance();
                    MailboxStorage mailboxStorage = Session.getInstance().getMailboxStorage();
                    Mailbox mailbox = mailboxStorage.getMailbox(Session.getInstance().getEmail());
                    if (mailbox == null) continue;
                    List<Email> emails = mailbox.getEmails();
                    List<String> uuids = emails.stream()
                            .map(Email::getId)
                            .toList();
                    NetworkPacket networkPacket =
                            serverConnection.emailUuidDiff(uuids, Session.getInstance().getEmail());
                    Type mapType = new TypeToken<List<String>>() {}.getType();
                    List<String> payload =
                            new Gson().fromJson(networkPacket.getJsonPayload(), mapType);
                    loadNewEmail(payload);

                } catch (Exception e) {
                    System.out.println("Polling error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void loadNewEmail(List<String> uuids) throws IOException {
        boolean update = false;
        for (String uuid : uuids) {
            update = true;
            NetworkPacket networkPacket = ServerConnection.getInstance().getEmail(uuid);
            MailboxStorage mailboxStorage = Session.getInstance().getMailboxStorage();
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> payload = new Gson().fromJson(networkPacket.getJsonPayload(), mapType);
            Email email = Email.from(payload);
            mailboxStorage.addEmailToDestination(Session.getInstance().getEmail(), email);
            updateEmailUi();
        }
        if (update) {
            showNotification();
        }
    }

    private void updateEmailUi() throws IOException {
        MailboxStorage mailboxStorage = Session.getInstance().getMailboxStorage();
        Mailbox mailbox = mailboxStorage.getMailbox(Session.getInstance().getEmail());
        List<Email> emails = mailbox.getEmails();
        Platform.runLater(() -> {
            observableEmails.setAll(emails);
        });
    }

    @FXML
    protected void onBtnNewEmailClick() {
        try {
            Email selectedEmail = emailListView
                    .getSelectionModel()
                    .getSelectedItem();
            NewEmailData newEmailData = new NewEmailData(NewEmailData.Mode.NEW, selectedEmail);
            running = false;
            SceneManager.switchScene("/fxml/newEmail.fxml", newEmailData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onBtnReplyEmailClick() {
        try {
            Email selectedEmail = emailListView
                    .getSelectionModel()
                    .getSelectedItem();
            NewEmailData newEmailData = new NewEmailData(NewEmailData.Mode.REPLY, selectedEmail);
            running = false;
            SceneManager.switchScene("/fxml/newEmail.fxml", newEmailData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onBtnReplyAllEmailClick() {
        try {
            Email selectedEmail = emailListView
                    .getSelectionModel()
                    .getSelectedItem();
            NewEmailData newEmailData = new NewEmailData(NewEmailData.Mode.REPLY_ALL, selectedEmail);
            running = false;
            SceneManager.switchScene("/fxml/newEmail.fxml", newEmailData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onBtnForwardEmailClick() {
        try {
            Email selectedEmail = emailListView
                    .getSelectionModel()
                    .getSelectedItem();
            NewEmailData newEmailData = new NewEmailData(NewEmailData.Mode.FORWARD, selectedEmail);
            running = false;
            SceneManager.switchScene("/fxml/newEmail.fxml", newEmailData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onBtnDeleteEmailClick() {
        Email selectedEmail = emailListView
                .getSelectionModel()
                .getSelectedItem();
        btnDeleteEmail.setDisable(true);
        new Thread(() -> {
            try {
                ServerConnection serverConnection = ServerConnection.getInstance();
                var serverResponse = serverConnection.deleteEmail(Session.getInstance().getEmail(), selectedEmail.getId());
                btnDeleteEmail.setDisable(false);
                if (serverResponse.isSuccess()) {
                    var res = MailboxStorage.getInstance().deleteEmail(Session.getInstance().getEmail(), selectedEmail.getId());
                    if (res) {
                        updateEmailUi();
                        Platform.runLater(() -> {
                            AlertUtil.showCorrect("Successfully deleted email");
                        });
                    }
                    else {
                        System.err.println("Failed to delete email");
                    }
                } else {
                    Platform.runLater(() -> {
                        AlertUtil.showError(serverResponse.getMessage());
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnDeleteEmail.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void setButtonsEnabled(boolean enabled) {
        btnNewEmail.setDisable(!enabled);
        btnReplyEmail.setDisable(!enabled);
        btnReplyAllEmail.setDisable(!enabled);
        btnForwardEmail.setDisable(!enabled);
        btnDeleteEmail.setDisable(!enabled);
    }

    private final AtomicBoolean notificationActive = new AtomicBoolean(false);

    private void showNotification() {
        if (!notificationActive.compareAndSet(false, true)) {
            return;
        }
        Platform.runLater(() -> {
            lblNotification.setText("New Email!");
            lblNotification.setVisible(true);
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> {
                lblNotification.setVisible(false);
                lblNotification.setText("");
                notificationActive.set(false);
            });

            pause.play();
        });
    }
}
