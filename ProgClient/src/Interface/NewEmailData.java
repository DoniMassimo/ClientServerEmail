package Interface;

import common.Email;

public class NewEmailData {
    public enum Mode {
        NEW,
        REPLY,
        REPLY_ALL,
        FORWARD
    }

    private Mode mode;
    private Email originalEmail;

    public NewEmailData(Mode mode, Email originalEmail) {
        this.mode = mode;
        this.originalEmail = originalEmail;
    }

    public Mode getMode() {
        return mode;
    }

    public Email getOriginalEmail() {
        return originalEmail;
    }
}
