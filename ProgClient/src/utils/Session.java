package utils;


import common.MailboxStorage;
import javafx.stage.Stage;

public class Session {
    private static Session instance;
    private String email;
    private Stage currentStage;
    private MailboxStorage mailboxStorage;

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public String getFilepath() {
        return "src/data/" + this.email.split("@")[0] + ".json";
    }

    public void setMailboxStorage(MailboxStorage mailboxStorage) {
        this.mailboxStorage = mailboxStorage;
    }

    public MailboxStorage getMailboxStorage() {
        return mailboxStorage;
    }
}
