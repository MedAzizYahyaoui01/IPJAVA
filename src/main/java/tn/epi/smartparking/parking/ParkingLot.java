package tn.epi.smartparking.parking;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** M2 - A car park: holds its spots and answers "what is free?" (F2.1, F2.2, F2.4, F3.2). */
public class ParkingLot {

    private final String name;
    private final Map<String, ParkingSpot> spots = new LinkedHashMap<>();

    public ParkingLot(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void addSpot(ParkingSpot spot) {                       // F2.2
        spots.put(spot.getId(), spot);
    }

    public void removeSpot(String spotId) {                        // F2.2
        spots.remove(spotId);
    }

    public Optional<ParkingSpot> findSpot(String spotId) {
        return Optional.ofNullable(spots.get(spotId));
    }

    public Collection<ParkingSpot> allSpots() {
        return spots.values();
    }

    public List<ParkingSpot> freeSpots(SpotType type) {           // F3.2, F4.1
        return spots.values().stream()
                .filter(ParkingSpot::isFree)
                .filter(s -> type == null || s.getType() == type)
                .toList();
    }

    public void putOutOfService(String spotId) {                   // F2.4
        findSpot(spotId).ifPresent(s -> s.setStatus(SpotStatus.OUT_OF_SERVICE));
    }

    public long count(SpotStatus status) {
        return spots.values().stream().filter(s -> s.getStatus() == status).count();
    }

    /** Builds a demo car park: 2 levels x 2 zones x 5 spots = 20 spots. */
    public static ParkingLot demo() {
        ParkingLot lot = new ParkingLot("SmartPark Centre");
        for (int level = 1; level <= 2; level++) {
            for (String zone : List.of("A", "B")) {
                for (int n = 1; n <= 5; n++) {
                    SpotType type = switch (n) {
                        case 1 -> SpotType.DISABLED;
                        case 2 -> SpotType.ELECTRIC;
                        default -> SpotType.STANDARD;
                    };
                    lot.addSpot(new ParkingSpot("L%d-%s-%02d".formatted(level, zone, n), level, zone, type));
                }
            }
        }
        return lot;
    }
}
