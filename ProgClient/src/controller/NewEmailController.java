package controller;

import Interface.DataReceiver;
import Interface.NewEmailData;
import common.Email;
import common.NetworkPacket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import network.ServerConnection;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewEmailController implements DataReceiver {
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSend;
    @FXML
    private TextField txtTo;
    @FXML
    private TextField txtSubject;
    @FXML
    private TextArea txtBody;
    @FXML
    private Label lblError;

    public void setData(Object data) {
        if (data instanceof NewEmailData newEmailData) {
            if (newEmailData.getMode() ==  NewEmailData.Mode.REPLY) {
                txtTo.setText(newEmailData.getOriginalEmail().getFrom());
                txtSubject.setText("Re: " + newEmailData.getOriginalEmail().getSubject());
            }
            else if (newEmailData.getMode() == NewEmailData.Mode.REPLY_ALL) {
                List<String> recipients = new ArrayList<>();

                recipients.add(newEmailData.getOriginalEmail().getFrom());
                recipients.addAll(newEmailData.getOriginalEmail().getTo());

                recipients.remove(Session.getInstance().getEmail());

                txtTo.setText(String.join(",", recipients));
                txtSubject.setText("Re: " + newEmailData.getOriginalEmail().getSubject());
            }
            else if (newEmailData.getMode() == NewEmailData.Mode.FORWARD) {
                txtSubject.setText(newEmailData.getOriginalEmail().getSubject());
                txtBody.setText(newEmailData.getOriginalEmail().getBody());
            }
        }
    }

    @FXML
    protected void onBtnCancelClick() {
        try {
            SceneManager.switchScene("/fxml/inbox.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onBtnSendClick() {
        List<String> emails = new ArrayList<>();
        for (String s : txtTo.getText().split(",")) {
            if (!Email.isValid(s.trim()))
            {
               lblError.setText("Invalid email");
               lblError.setTextFill(Color.RED);
               lblError.setVisible(true);
               return;
            }
            emails.add(s.trim());
        }

        btnSend.setDisable(true);
        new Thread(() -> {
            try {
                Email email = new Email(Session.getInstance().getEmail(), emails, txtSubject.getText(), txtBody.getText());
                ServerConnection serverConnection = ServerConnection.getInstance();
                NetworkPacket serverResponse = serverConnection.sendEmail(email);

                Platform.runLater(() -> {
                    btnSend.setDisable(false);
                    if (serverResponse.isSuccess()) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Message sent successfully");
                        alert.showAndWait();
                        try {
                            SceneManager.switchScene("/fxml/inbox.fxml");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        lblError.setText("Error");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnSend.setDisable(false);
                    lblError.setText("Server not reachable");
                });
            }
        }).start();
    }
}
