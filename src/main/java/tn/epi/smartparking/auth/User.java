package tn.epi.smartparking.auth;

import java.util.Set;

/** M1 - An account. A record: immutable data with equals/hashCode/toString for free. */
public record User(String email, String password, String fullName, Role role, Set<String> plates) {

    public User {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        plates = Set.copyOf(plates);   // defensive, immutable copy
    }

    public boolean owns(String plate) {
        return plates.contains(plate);
    }
}
