package common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Mailbox {
    private String name;
    private List<Email> emails = new ArrayList<>();

    public Mailbox(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void addEmail(Email email) {
        emails.add(email);
    }

    public List<Email> differenceByIds(List<String> ids) {
        return emails.stream()
                .filter(email -> !ids.contains(email.getId()))
                .collect(Collectors.toList());
    }

    public Email getEmailById(String id) {
        return emails.stream()
                .filter(email -> email.getId().equals(id))
                .findFirst()
                .orElse(null); // oppure lanciare eccezione
    }
}