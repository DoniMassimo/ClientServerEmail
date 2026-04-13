package controller;

import common.MailboxStorage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import common.Email;
import network.ServerConnection;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField txtFieldEmail;

    @FXML
    private Label lblError;

    @FXML
    private Button btnLogin;

    @FXML
    protected void onBtnLoginClick() {
        String email = txtFieldEmail.getText().trim();
        if (!Email.isValid(email)) {
            lblError.setText("Invalid email");
            return;
        }
        btnLogin.setDisable(true);
        new Thread(() -> {
            try {
                ServerConnection serverConnection = ServerConnection.getInstance();
                var serverResponse = serverConnection.checkUser(email);
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    if (serverResponse.isSuccess()) {
                        lblError.setText("");
                        try {
                            Session.getInstance().setEmail(email);
                            Session.getInstance().setMailboxStorage(
                                    MailboxStorage.getInstance(Session.getInstance().getFilepath()));
                            SceneManager.switchScene("/fxml/inbox.fxml");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        lblError.setText("Login Failed");
                        lblError.setVisible(true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    lblError.setText("Server not reachable");
                    lblError.setVisible(true);
                });
            }
        }).start();
    }
}
