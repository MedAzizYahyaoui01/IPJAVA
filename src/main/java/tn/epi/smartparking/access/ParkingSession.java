package tn.epi.smartparking.access;

import java.time.LocalDateTime;

/**
 * M5 - One stay in the car park, from entry to exit.
 * Sealed so a session is either Active or Closed, nothing else.
 */
public sealed interface ParkingSession {

    String ticketId();
    String plate();
    LocalDateTime entry();

    record Active(String ticketId, String plate, LocalDateTime entry, boolean visitor)
            implements ParkingSession {}

    record Closed(String ticketId, String plate, LocalDateTime entry, LocalDateTime exit, double fee)
            implements ParkingSession {}
}
