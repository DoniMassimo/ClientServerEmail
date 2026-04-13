package common;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkPacket {
    private String action;
    private String sender;
    private String jsonPayload;
    private boolean success;
    private String message;

    public NetworkPacket(String action, String sender, String jsonPayload) {
        this.action = action;
        this.sender = sender;
        this.jsonPayload = jsonPayload;
    }

    public NetworkPacket(boolean success, String jsonPayload, String message) {
        this.success = success;
        this.jsonPayload = jsonPayload;
        this.message = message;
    }

    public static NetworkPacket checkUserReq(String email) {
        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        String payload = new Gson().toJson(data);
        return new NetworkPacket("CHECK_USER", "client", payload);
    }

    public static NetworkPacket sendEmailReq(Email email) {
        Map<String, Object> data = new HashMap<>();
        data.put("from", email.getFrom());
        data.put("id", email.getId());
        data.put("to", email.getTo());
        data.put("subject", email.getSubject());
        data.put("body", email.getBody());
        data.put("date", email.getDate());
        String payload = new Gson().toJson(data);
        return new NetworkPacket("SEND_EMAIL", "client", payload);
    }

    public static NetworkPacket sendEmailRes(Email email) {
        Map<String, Object> data = new HashMap<>();
        data.put("from", email.getFrom());
        data.put("id", email.getId());
        data.put("to", email.getTo());
        data.put("subject", email.getSubject());
        data.put("body", email.getBody());
        data.put("date", email.getDate());
        String payload = new Gson().toJson(data);
        return new NetworkPacket(true, payload, "");
    }

    public static NetworkPacket uuidDiffReq(List<String> uuids, String email) {
        List<String> payloadList = new ArrayList<>(uuids);
        payloadList.add(0, email);
        String payload = new Gson().toJson(payloadList);
        return new NetworkPacket("EMAIL_DIFF", "client", payload);
    }

    public static NetworkPacket getEmailReq(String uuid, String email) {
        List<String> payloadList = new ArrayList<>();
        payloadList.add(0, uuid);
        payloadList.add(1, email);
        String payload = new Gson().toJson(payloadList);
        return new NetworkPacket("GET_EMAIL", "client", payload);
    }

    public static NetworkPacket deleteEmailReq(String uuid, String email) {
        List<String> payloadList = new ArrayList<>();
        payloadList.add(0, email);
        payloadList.add(1, uuid);
        String payload = new Gson().toJson(payloadList);
        return new NetworkPacket("DELETE", "client", payload);
    }

    public static NetworkPacket pingReq() {
        return new NetworkPacket("PING", "client", null);
    }

    public String getAction() {
        return action;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}