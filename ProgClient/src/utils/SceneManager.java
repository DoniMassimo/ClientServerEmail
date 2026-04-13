package utils;

import Interface.DataReceiver;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    public static void switchScene(String viewName) throws IOException {
        URL loadedView = SceneManager.class.getResource(viewName);

        Parent root = FXMLLoader.load(loadedView);
        Scene scene = new Scene(root);

        Stage currentStage = Session.getInstance().getCurrentStage();
        currentStage.setScene(scene);
        currentStage.show();
    }

    public static void switchScene(String viewName, Object data) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(viewName));
        Parent root = loader.load();
        Object controller = loader.getController();
        if (controller instanceof DataReceiver) {
            ((DataReceiver) controller).setData(data);
        }
        Scene scene = new Scene(root);
        Stage currentStage = Session.getInstance().getCurrentStage();
        currentStage.setScene(scene);
        currentStage.show();
    }
}