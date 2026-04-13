package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerModel {

    private final ObservableList<String> logs =
            FXCollections.observableArrayList();

    public ObservableList<String> getLogs() {
        return logs;
    }

    public void addLog(String log) {
        logs.add(log);
    }
}
