package tn.epi.smartparking.access;

import tn.epi.smartparking.auth.UserService;
import tn.epi.smartparking.billing.BillingService;
import tn.epi.smartparking.reservation.ReservationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** M5 - Entry and exit at the barriers (workflow 6.1: F5.1, F5.2, F5.3, F6.1). */
public class AccessService {

    private final UserService users;
    private final ReservationService reservations;
    private final BillingService billing;
    private final Map<String, ParkingSession.Active> activeByPlate = new HashMap<>();
    private final AtomicInteger tickets = new AtomicInteger();

    public AccessService(UserService users, ReservationService reservations, BillingService billing) {
        this.users = users;
        this.reservations = reservations;
        this.billing = billing;
    }

    /** The camera read a plate at the entry. Returns the opened session. */
    public ParkingSession.Active enter(String plate, LocalDateTime now) {
        reservations.checkIn(plate);                                            // consume reservation if any
        boolean known = users.findByPlate(plate).isPresent();                   // "Plate known?"
        String ticket = (known ? "S" : "T") + tickets.incrementAndGet();        // T = visitor ticket (F5.2)
        var session = new ParkingSession.Active(ticket, plate, now, !known);
        activeByPlate.put(plate, session);
        return session;                                                          // barrier opens
    }

    /** The camera read a plate at the exit. Computes the fee and closes the session (F5.3, F6.1). */
    public ParkingSession.Closed exit(String plate, LocalDateTime now) {
        ParkingSession.Active s = activeByPlate.remove(plate);
        if (s == null) {
            throw new IllegalStateException("No active session for plate " + plate);
        }
        double fee = billing.computeFee(s.entry(), now);
        return new ParkingSession.Closed(s.ticketId(), plate, s.entry(), now, fee);
    }

    public int vehiclesInside() {
        return activeByPlate.size();
    }
}
