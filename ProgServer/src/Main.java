import controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import network.ServerListener;

import java.io.IOException;
import java.net.ServerSocket;

public class Main extends Application {

    private String landingPage = "fxml/server.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader root = new FXMLLoader(Main.class.getResource(landingPage));
        Scene scene = new Scene(root.load());
        stage.setScene(scene);
        stage.show();
        ServerController serverController = root.getController();
        ServerSocket serverSocket = new ServerSocket(8080);
        Thread serverThread = new Thread(new ServerListener(serverSocket, serverController));
        serverThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


