package tn.epi.smartparking.reservation;

import org.junit.jupiter.api.Test;
import tn.epi.smartparking.notification.NotificationService;
import tn.epi.smartparking.parking.ParkingLot;
import tn.epi.smartparking.parking.SpotStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private final ParkingLot lot = ParkingLot.demo();
    private final ReservationService service = new ReservationService(lot, new NotificationService());
    private final LocalDateTime nine = LocalDateTime.of(2026, 10, 26, 9, 0);

    @Test
    void reservingASpotMarksItReserved() {
        service.reserve("d@x.tn", "123 TU 1", "L1-A-03", nine, nine.plusHours(2));
        assertEquals(SpotStatus.RESERVED, lot.findSpot("L1-A-03").orElseThrow().getStatus());
    }

    @Test
    void cannotReserveATakenSpot() {
        service.reserve("d@x.tn", "123 TU 1", "L1-A-03", nine, nine.plusHours(2));
        assertThrows(IllegalStateException.class,
                () -> service.reserve("e@x.tn", "456 TU 2", "L1-A-03", nine, nine.plusHours(1)));
    }

    @Test
    void noShowExpiresAfter15MinutesAndFreesTheSpot() {
        service.reserve("d@x.tn", "123 TU 1", "L1-A-03", nine, nine.plusHours(2));
        assertEquals(0, service.expireOverdue(nine.plusMinutes(10)));
        assertEquals(1, service.expireOverdue(nine.plusMinutes(16)));
        assertEquals(SpotStatus.FREE, lot.findSpot("L1-A-03").orElseThrow().getStatus());
    }

    @Test
    void cancelFreesTheSpot() {
        Reservation r = service.reserve("d@x.tn", "123 TU 1", "L1-A-03", nine, nine.plusHours(2));
        service.cancel(r.id());
        assertTrue(lot.findSpot("L1-A-03").orElseThrow().isFree());
    }
}
