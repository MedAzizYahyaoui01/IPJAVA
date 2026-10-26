package tn.epi.smartparking.notification;

import java.util.ArrayList;
import java.util.List;

/** M8 - Messages to drivers and agents (F8.1, F8.2). Console for now; email/SMS later. */
public class NotificationService {

    private final List<String> log = new ArrayList<>();

    public void notifyDriver(String email, String message) {
        send("[DRIVER " + email + "] " + message);
    }

    public void alertAgents(String message) {
        send("[AGENT ALERT] " + message);
    }

    public List<String> sent() {
        return List.copyOf(log);
    }

    private void send(String line) {
        log.add(line);
        System.out.println("  >> " + line);
    }
}
