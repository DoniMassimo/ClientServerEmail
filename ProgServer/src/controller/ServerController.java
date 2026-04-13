package controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import model.ServerModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class ServerController {
    @FXML
    private VBox vbox;
    private final ServerModel model = new ServerModel();

    @FXML
    public void initialize() {
        model.getLogs().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String log : change.getAddedSubList()) {
                        vbox.getChildren().add(new Label(log));
                    }
                }
            }
        });
    }

    public void addLog(String log) {
        Platform.runLater(() -> model.getLogs().add(log));
    }

    public void onBtnTestClick() {
        addLog("ciaooo");
    }
}
