package tn.epi.smartparking.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** M1 - Accounts and login (F1.1, F1.2, F1.4). In-memory for now, JDBC later. */
public class UserService {

    private final Map<String, User> usersByEmail = new HashMap<>();

    public User register(String email, String password, String fullName, Set<String> plates) {   // F1.1
        if (usersByEmail.containsKey(email)) {
            throw new IllegalStateException("Email already used: " + email);
        }
        User user = new User(email, password, fullName, Role.DRIVER, plates);
        usersByEmail.put(email, user);
        return user;
    }

    public User createStaff(String email, String password, String fullName, Role role) {           // F1.4
        User user = new User(email, password, fullName, role, Set.of());
        usersByEmail.put(email, user);
        return user;
    }

    public Optional<User> login(String email, String password) {                                    // F1.2
        return Optional.ofNullable(usersByEmail.get(email))
                .filter(u -> u.password().equals(password));
    }

    /** Used by the plate camera: who owns this plate? (F5.1) */
    public Optional<User> findByPlate(String plate) {
        return usersByEmail.values().stream().filter(u -> u.owns(plate)).findFirst();
    }
}
