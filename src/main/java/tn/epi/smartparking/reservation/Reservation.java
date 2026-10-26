package tn.epi.smartparking.reservation;

import java.time.Duration;
import java.time.LocalDateTime;

/** M4 - A booking of one spot by one driver. */
public record Reservation(String id, String driverEmail, String plate, String spotId,
                          LocalDateTime start, LocalDateTime end) {

    /** The spot is held this long after the start time before the reservation expires (F4.4). */
    public static final Duration GRACE_PERIOD = Duration.ofMinutes(15);

    public Reservation {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be after start");
        }
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(start.plus(GRACE_PERIOD));
    }
}
