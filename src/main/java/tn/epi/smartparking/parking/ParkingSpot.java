package tn.epi.smartparking.parking;

/**
 * M2 - One parking spot. Its identity (id, level, zone, type) never changes;
 * only its status moves, driven by sensors, reservations and agents.
 */
public class ParkingSpot {

    private final String id;      // e.g. "L1-A-03"
    private final int level;
    private final String zone;
    private final SpotType type;
    private SpotStatus status = SpotStatus.FREE;

    public ParkingSpot(String id, int level, String zone, SpotType type) {
        this.id = id;
        this.level = level;
        this.zone = zone;
        this.type = type;
    }

    public String getId()         { return id; }
    public int getLevel()         { return level; }
    public String getZone()       { return zone; }
    public SpotType getType()     { return type; }
    public SpotStatus getStatus() { return status; }

    public void setStatus(SpotStatus status) { this.status = status; }

    public boolean isFree() { return status == SpotStatus.FREE; }

    @Override
    public String toString() {
        return "%s [%s, level %d, zone %s] %s".formatted(id, type, level, zone, status);
    }
}
