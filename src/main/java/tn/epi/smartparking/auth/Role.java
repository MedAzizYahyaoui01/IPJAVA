package tn.epi.smartparking.auth;

/** M1 - Human actors with an account (the Visitor has none). */
public enum Role {
    DRIVER, AGENT, ADMIN;

    /** An admin can do everything an agent does (section 5 of the report). */
    public boolean includes(Role other) {
        return this == other || (this == ADMIN && other == AGENT);
    }
}
