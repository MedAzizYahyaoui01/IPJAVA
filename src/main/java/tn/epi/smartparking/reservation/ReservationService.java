package tn.epi.smartparking.reservation;

import tn.epi.smartparking.notification.NotificationService;
import tn.epi.smartparking.parking.ParkingLot;
import tn.epi.smartparking.parking.ParkingSpot;
import tn.epi.smartparking.parking.SpotStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** M4 - Reserve, cancel and expire reservations (F4.2, F4.3, F4.4, F8.1). */
public class ReservationService {

    private final ParkingLot lot;
    private final NotificationService notifications;
    private final List<Reservation> active = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger();

    public ReservationService(ParkingLot lot, NotificationService notifications) {
        this.lot = lot;
        this.notifications = notifications;
    }

    public Reservation reserve(String driverEmail, String plate, String spotId,
                               LocalDateTime start, LocalDateTime end) {                       // F4.2
        ParkingSpot spot = lot.findSpot(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown spot " + spotId));
        if (!spot.isFree()) {
            throw new IllegalStateException("Spot " + spotId + " is " + spot.getStatus());
        }
        Reservation r = new Reservation("R" + counter.incrementAndGet(), driverEmail, plate, spotId, start, end);
        spot.setStatus(SpotStatus.RESERVED);
        active.add(r);
        notifications.notifyDriver(driverEmail, "Spot %s reserved from %s".formatted(spotId, start));
        return r;
    }

    public void cancel(String reservationId) {                                                 // F4.3
        findById(reservationId).ifPresent(this::release);
    }

    /** Called by the scheduler every minute (F4.4). Returns how many expired. */
    public int expireOverdue(LocalDateTime now) {
        List<Reservation> overdue = active.stream().filter(r -> r.isExpired(now)).toList();
        overdue.forEach(r -> {
            release(r);
            notifications.notifyDriver(r.driverEmail(), "Reservation " + r.id() + " expired (no-show)");
        });
        return overdue.size();
    }

    /** At the entry barrier: is there a reservation for this plate? Consumes it if so (F5.1). */
    public Optional<Reservation> checkIn(String plate) {
        Optional<Reservation> r = active.stream().filter(x -> x.plate().equals(plate)).findFirst();
        r.ifPresent(active::remove);
        return r;
    }

    public List<Reservation> activeReservations() {
        return List.copyOf(active);
    }

    private Optional<Reservation> findById(String id) {
        return active.stream().filter(r -> r.id().equals(id)).findFirst();
    }

    private void release(Reservation r) {
        active.remove(r);
        lot.findSpot(r.spotId())
                .filter(s -> s.getStatus() == SpotStatus.RESERVED)
                .ifPresent(s -> s.setStatus(SpotStatus.FREE));
    }
}
