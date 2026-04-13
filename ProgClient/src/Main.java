
import common.NetworkPacket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.Session;


import java.io.IOException;

public class Main extends Application {

    private String landingPage = "fxml/login.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(landingPage));
        Scene scene = new Scene(root);
        Session.getInstance().setCurrentStage(stage);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}