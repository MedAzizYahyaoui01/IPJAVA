package tn.epi.smartparking.sensor;

import org.junit.jupiter.api.Test;
import tn.epi.smartparking.analytics.AnalyticsService;
import tn.epi.smartparking.notification.NotificationService;
import tn.epi.smartparking.parking.ParkingLot;
import tn.epi.smartparking.parking.SpotStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SensorServiceTest {

    private final ParkingLot lot = ParkingLot.demo();
    private final NotificationService notifications = new NotificationService();
    private final SensorService sensors = new SensorService(lot, notifications, new AnalyticsService());
    private final LocalDateTime now = LocalDateTime.of(2026, 10, 26, 10, 0);

    @Test
    void carDetectedThenLeft() {
        sensors.handle(new SensorEvent.CarDetected("L1-A-04", now));
        assertEquals(SpotStatus.OCCUPIED, lot.findSpot("L1-A-04").orElseThrow().getStatus());
        sensors.handle(new SensorEvent.CarLeft("L1-A-04", now.plusHours(1)));
        assertEquals(SpotStatus.FREE, lot.findSpot("L1-A-04").orElseThrow().getStatus());
    }

    @Test
    void faultPutsSpotOutOfServiceAndAlertsAgents() {
        sensors.handle(new SensorEvent.SensorFault("L2-B-01", now, "no signal"));
        assertEquals(SpotStatus.OUT_OF_SERVICE, lot.findSpot("L2-B-01").orElseThrow().getStatus());
        assertTrue(notifications.sent().stream().anyMatch(m -> m.contains("Sensor fault")));
    }
}
