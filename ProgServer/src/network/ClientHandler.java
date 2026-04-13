package network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.Email;
import common.Mailbox;
import common.MailboxStorage;
import common.NetworkPacket;
import controller.ServerController;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private Socket socket;
    private final Gson gson = new Gson();
    private ServerController serverController;

    public ClientHandler(Socket socket, ServerController serverController) {
        this.socket = socket;
        this.serverController = serverController;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String inputLine = in.readLine();
            NetworkPacket request = gson.fromJson(inputLine, NetworkPacket.class);

            NetworkPacket response;

            switch (request.getAction()) {
                case "CHECK_USER":
                    response = checkUser(request);
                    break;
                case "SEND_EMAIL":
                    response = sendEmail(request);
                    break;
                case "PING":
                    response = new NetworkPacket(true, null, null);
                    break;
                case "EMAIL_DIFF":
                    response = emailDiff(request);
                    break;
                case "GET_EMAIL":
                    response = getEmail(request);
                    break;
                case "DELETE":
                    response = deleteEmail(request);
                    break;
                default:
                    response = new NetworkPacket(false, null, "Azione sconosciuta");
            }

            out.println(gson.toJson(response));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private NetworkPacket deleteEmail(NetworkPacket request) throws IOException {
        Type mapType = new TypeToken<List<String>>(){}.getType();
        List<String> payload = gson.fromJson(request.getJsonPayload(), mapType);
        MailboxStorage mailboxStorage = MailboxStorage.getInstance();
        if (!mailboxStorage.userExists(payload.getFirst())) {
            serverController.addLog("DELETE: " + payload.getFirst() + " does non exist -> INVALID");
            return new NetworkPacket(false, null, "Invalid user");
        }
        if (mailboxStorage.deleteEmail(payload.getFirst(), payload.get(1))) {
            serverController.addLog("DELETE: " + payload.getFirst() + " -> VALID");
            return new NetworkPacket(true, null, "Email successfully removed");
        }
        return new NetworkPacket(false, null, "Email id does not exist");
    }

    private NetworkPacket getEmail(NetworkPacket request) throws IOException {
        Type mapType = new TypeToken<List<String>>(){}.getType();
        List<String> payload = gson.fromJson(request.getJsonPayload(), mapType);
        MailboxStorage mailboxStorage = MailboxStorage.getInstance();
        if (!mailboxStorage.userExists(payload.get(1))) {
            serverController.addLog("GET_EMAIL: " + payload.get(1) + " does non exist -> INVALID");
            return new NetworkPacket(false, null, "Invalid user");
        }
        Mailbox mailbox = mailboxStorage.getMailbox(payload.get(1));
        if (mailbox == null) {
            serverController.addLog("GET_EMAIL: " + payload.get(1) + " does non exist -> INVALID");
            return new NetworkPacket(false, null, "Invalid user");
        }
        Email email = mailbox.getEmailById(payload.get(0));
        if (email == null) {
            serverController.addLog("GET_EMAIL: " + payload.get(0) + " does non exist -> INVALID");
            return new NetworkPacket(false, null, "Invalid uuid");
        }
        return NetworkPacket.sendEmailRes(email);
    }

    private NetworkPacket checkUser(NetworkPacket request) throws IOException {
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> email = gson.fromJson(request.getJsonPayload(), mapType);
        MailboxStorage mailboxStorage = MailboxStorage.getInstance();
        if (mailboxStorage.userExists(email.get("email"))) {
            serverController.addLog("CHECK_USER: " + email.get("email") + " -> VALID");
            return new NetworkPacket(true, null, "");
        } else {
            serverController.addLog("CHECK_USER: " + email.get("email") + " -> INVALID");
            return new NetworkPacket(false, null, "User doesn't exist");
        }
    }

    private NetworkPacket sendEmail(NetworkPacket request) throws IOException {
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> payload = gson.fromJson(request.getJsonPayload(), mapType);
        Email email = Email.from(payload);
        MailboxStorage mailboxStorage = MailboxStorage.getInstance();
        if (!mailboxStorage.userExists(email.getFrom())) {
            serverController.addLog("SEND_EMAIL: sender " + email.getFrom() + " does not exist -> INVALID");
            return new NetworkPacket(false, null, "");
        }
        for (String user : email.getTo()) {
            if (!mailboxStorage.userExists(user)) {
                serverController.addLog(String.format("SEND_EMAIL: to %s -> INVALID (user does not exist)", user));
                return new NetworkPacket(false, null, "User" + user + "doesn't exist");
            }
        }
        for (String user : email.getTo()) {
            mailboxStorage.addEmailToDestination(user, email);
        }
        serverController.addLog(String.format("SEND_EMAIL: form %s -> VALID", email.getFrom()));
        return new NetworkPacket(true, null, "");
    }

    private NetworkPacket emailDiff(NetworkPacket request) throws IOException {
        Type mapType = new TypeToken<List<String>>(){}.getType();
        List<String> payload = gson.fromJson(request.getJsonPayload(), mapType);
        String email = payload.removeFirst();
        MailboxStorage mailboxStorage = MailboxStorage.getInstance();
        Mailbox mailbox = mailboxStorage.getMailbox(email);
        List<Email> emails = mailbox.differenceByIds(payload);
        List<String> diff = emails.stream()
                .map(Email::getId)
                .collect(Collectors.toList());
        String retPayload = new Gson().toJson(diff);
        return new NetworkPacket(true, retPayload, "");
    }
}