package tn.epi.smartparking.analytics;

import tn.epi.smartparking.parking.ParkingLot;
import tn.epi.smartparking.parking.SpotStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/** M7 - Occupancy statistics and availability forecast (F7.1, F7.3, F7.4). */
public class AnalyticsService {

    /** One measure of the car park at one moment. */
    public record Snapshot(LocalDateTime at, double occupancyRate) {}

    private final List<Snapshot> history = new ArrayList<>();

    public void recordSnapshot(ParkingLot lot, LocalDateTime at) {
        history.add(new Snapshot(at, occupancyRate(lot)));
    }

    /** F7.1 - Share of usable spots that are occupied or reserved, 0.0 to 1.0. */
    public static double occupancyRate(ParkingLot lot) {
        long total = lot.allSpots().size() - lot.count(SpotStatus.OUT_OF_SERVICE);
        if (total == 0) return 0;
        long used = lot.count(SpotStatus.OCCUPIED) + lot.count(SpotStatus.RESERVED);
        return (double) used / total;
    }

    /**
     * F7.3 - Forecast occupancy at a given hour = average of past snapshots taken at that hour.
     * Simple and explainable; can later be replaced by a weighted or ML model.
     */
    public OptionalDouble forecast(int hour) {
        return history.stream()
                .filter(s -> s.at().getHour() == hour)
                .mapToDouble(Snapshot::occupancyRate)
                .average();
    }

    /** F7.4 - Suggest a peak multiplier when the forecast is above 80 %. */
    public double suggestedMultiplier(int hour) {
        double f = forecast(hour).orElse(0);
        return f >= 0.8 ? 1.5 : f >= 0.6 ? 1.2 : 1.0;
    }

    public void addHistory(Snapshot s) {
        history.add(s);
    }
}
