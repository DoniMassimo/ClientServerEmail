package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.UUID;

public class Email {
    private String id;
    private String from;
    private List<String> to;
    private String subject;
    private String body;
    private String date;

    public Email() {
        this.id = UUID.randomUUID().toString();
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public Email(String from, List<String> to, String subject, String body) {
        this();
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    private Email(String id, String date, String from, List<String> to, String subject, String body) {
        this.date = date;
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public static Email from(Map<String, Object> payload) {
        return new Email(payload.get("id").toString(), payload.get("date").toString(), payload.get("from").toString(),
                (List<String>)payload.get("to"), payload.get("subject").toString(), payload.get("body").toString());
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}