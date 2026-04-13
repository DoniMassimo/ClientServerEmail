package common;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MailboxStorage {
    private static MailboxStorage instance;
    private String filePath = "src/data/data.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();

    private MailboxStorage() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                saveAll(new ArrayList<>()); // Crea file vuoto []
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private MailboxStorage(String filePath) {
        this.filePath = filePath;

        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                saveAll(new ArrayList<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized MailboxStorage getInstance() {
        if (instance == null) instance = new MailboxStorage();
        return instance;
    }

    public static synchronized MailboxStorage getInstance(String filePath) {
        if (instance == null) instance = new MailboxStorage(filePath);
        return instance;
    }

    private List<Mailbox> loadAll() throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            Type type = new TypeToken<List<Mailbox>>(){}.getType();
            List<Mailbox> data = gson.fromJson(reader, type);
            return (data != null) ? data : new ArrayList<>();
        }
    }

    private void saveAll(List<Mailbox> data) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        }
    }

    public Mailbox getMailbox(String email) throws IOException {
        globalLock.readLock().lock();
        try {
            List<Mailbox> all = loadAll();
            return all.stream()
                    .filter(m -> m.getName().equals(email))
                    .findFirst()
                    .orElse(null);
        } finally {
            globalLock.readLock().unlock();
        }
    }

    public void addEmailToDestination(String recipientEmail, Email email) throws IOException {
        globalLock.writeLock().lock();
        try {
            List<Mailbox> all = loadAll();
            boolean found = false;

            for (Mailbox m : all) {
                if (m.getName().equals(recipientEmail)) {
                    m.addEmail(email);
                    found = true;
                    break;
                }
            }

            if (found) {
                saveAll(all);
            } else {
                throw new IOException("Destinatario non trovato sul server.");
            }
        } finally {
            globalLock.writeLock().unlock();
        }
    }

    public boolean userExists(String email) throws IOException {
        globalLock.readLock().lock();
        try {
            List<Mailbox> all = loadAll();
            return all.stream()
                    .anyMatch(m -> m.getName().equalsIgnoreCase(email));
        } finally {
            globalLock.readLock().unlock();
        }
    }

    public boolean deleteEmail(String userEmail, String emailId) throws IOException {
        globalLock.writeLock().lock();
        try {
            List<Mailbox> all = loadAll();
            boolean removed = false;
            for (Mailbox mailbox : all) {
                if (mailbox.getName().equals(userEmail)) {
                    Iterator<Email> iterator = mailbox.getEmails().iterator();
                    while (iterator.hasNext()) {
                        Email email = iterator.next();
                        if (email.getId().equals(emailId)) {
                            iterator.remove();
                            removed = true;
                            break;
                        }
                    }
                    break;
                }
            }
            if (removed) {
                saveAll(all);
            }
            return removed;
        } finally {
            globalLock.writeLock().unlock();
        }
    }
}