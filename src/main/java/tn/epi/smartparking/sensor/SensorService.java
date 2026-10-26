package tn.epi.smartparking.sensor;

import tn.epi.smartparking.analytics.AnalyticsService;
import tn.epi.smartparking.notification.NotificationService;
import tn.epi.smartparking.parking.ParkingLot;
import tn.epi.smartparking.parking.SpotStatus;
import tn.epi.smartparking.sensor.SensorEvent.CarDetected;
import tn.epi.smartparking.sensor.SensorEvent.CarLeft;
import tn.epi.smartparking.sensor.SensorEvent.SensorFault;

/** M3 - Applies sensor events to the live map of the car park (F3.1, F3.3, F8.2). */
public class SensorService {

    private final ParkingLot lot;
    private final NotificationService notifications;
    private final AnalyticsService analytics;

    public SensorService(ParkingLot lot, NotificationService notifications, AnalyticsService analytics) {
        this.lot = lot;
        this.notifications = notifications;
        this.analytics = analytics;
    }

    public void handle(SensorEvent event) {
        // Java 21: pattern matching for switch + record patterns
        switch (event) {
            case CarDetected(String spotId, var at) ->
                    lot.findSpot(spotId).ifPresent(s -> s.setStatus(SpotStatus.OCCUPIED));
            case CarLeft(String spotId, var at) ->
                    lot.findSpot(spotId).ifPresent(s -> s.setStatus(SpotStatus.FREE));
            case SensorFault(String spotId, var at, String reason) -> {
                lot.putOutOfService(spotId);
                notifications.alertAgents("Sensor fault on " + spotId + ": " + reason);
            }
        }
        analytics.recordSnapshot(lot, event.at());

        if (lot.count(SpotStatus.FREE) == 0) {
            notifications.alertAgents("Car park FULL");
        }
    }
}
