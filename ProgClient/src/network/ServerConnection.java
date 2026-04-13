package network;

import com.google.gson.Gson;
import common.Email;
import common.NetworkPacket;
import utils.Session;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class ServerConnection {
    private static ServerConnection instance;
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private final Gson gson = new Gson();

    public static synchronized ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    public NetworkPacket checkUser(String email) throws IOException {
        NetworkPacket networkPacket = NetworkPacket.checkUserReq(email);
        return send(networkPacket);
    }

    public NetworkPacket sendEmail(Email email) throws IOException {
        NetworkPacket networkPacket = NetworkPacket.sendEmailReq(email);
        return send(networkPacket);
    }

    public NetworkPacket ping() throws IOException {
        NetworkPacket networkPacket = NetworkPacket.pingReq();
        return send(networkPacket);
    }

    public NetworkPacket emailUuidDiff(List<String> uuids, String email) throws IOException {
        NetworkPacket networkPacket = NetworkPacket.uuidDiffReq(uuids, email);
        return send(networkPacket);
    }

    public NetworkPacket getEmail(String uuid)  throws IOException {
        NetworkPacket networkPacket = NetworkPacket.getEmailReq(uuid, Session.getInstance().getEmail());
        return send(networkPacket);
    }

    public NetworkPacket deleteEmail(String email, String uuid)  throws IOException {
        NetworkPacket networkPacket = NetworkPacket.deleteEmailReq(uuid, email);
        return send(networkPacket);
    }

    public NetworkPacket send(NetworkPacket request) throws IOException {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(gson.toJson(request));

            String responseJson = in.readLine();
            return gson.fromJson(responseJson, NetworkPacket.class);
        }
    }
}