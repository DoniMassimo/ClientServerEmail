package network;

import controller.ServerController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerListener extends Thread {
    private ServerSocket serverSocket;
    private ServerController serverController;

    public ServerListener(ServerSocket serverSocket, ServerController serverController) {
        this.serverSocket = serverSocket;
        this.serverController = serverController;
    }

    @Override
    public void run() {
        while (true) {
            Socket client = null;
            try {
                client = serverSocket.accept();
                new Thread(new ClientHandler(client, serverController)).start();
            } catch (IOException e) {
                serverController.addLog("server exception");
            }
        }
    }
}
