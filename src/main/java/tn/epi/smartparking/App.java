package tn.epi.smartparking;

import tn.epi.smartparking.access.AccessService;
import tn.epi.smartparking.analytics.AnalyticsService;
import tn.epi.smartparking.auth.UserService;
import tn.epi.smartparking.billing.BillingService;
import tn.epi.smartparking.billing.PaymentMethod;
import tn.epi.smartparking.billing.Tariff;
import tn.epi.smartparking.notification.NotificationService;
import tn.epi.smartparking.parking.ParkingLot;
import tn.epi.smartparking.parking.SpotStatus;
import tn.epi.smartparking.reservation.ReservationService;
import tn.epi.smartparking.sensor.SensorEvent;
import tn.epi.smartparking.sensor.SensorService;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;

/**
 * SmartPark - console entry point.
 * Run:  mvn compile exec:java              (interactive menu)
 *       mvn compile exec:java -Dexec.args="--demo"   (scripted scenario)
 */
public class App {

    private final ParkingLot lot = ParkingLot.demo();
    private final NotificationService notifications = new NotificationService();
    private final AnalyticsService analytics = new AnalyticsService();
    private final UserService users = new UserService();
    private final BillingService billing = new BillingService(Tariff.standard());
    private final ReservationService reservations = new ReservationService(lot, notifications);
    private final AccessService access = new AccessService(users, reservations, billing);
    private final SensorService sensors = new SensorService(lot, notifications, analytics);

    public static void main(String[] args) {
        App app = new App();
        if (args.length > 0 && args[0].equals("--demo")) {
            app.demo();
        } else {
            app.menu();
        }
    }

    /** Plays workflows 6.1 and 6.2 from the project report, step by step. */
    void demo() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 26, 8, 30);
        System.out.println("=== SmartPark demo: " + lot.getName() + " ===");
        printMap();

        System.out.println("\n[M1] Driver registers with plate 123 TU 4567");
        users.register("aziz@example.com", "secret", "Aziz", Set.of("123 TU 4567"));

        System.out.println("\n[M4] Driver reserves L1-A-03 for 09:00-11:00");
        var r = reservations.reserve("aziz@example.com", "123 TU 4567", "L1-A-03",
                t.withHour(9).withMinute(0), t.withHour(11).withMinute(0));
        System.out.println("  Reservation " + r.id() + " -> spot " + r.spotId());

        System.out.println("\n[M5] 08:55 known plate at entry -> barrier opens");
        var s1 = access.enter("123 TU 4567", t.withHour(8).withMinute(55));
        System.out.println("  Session " + s1.ticketId() + " (visitor=" + s1.visitor() + ")");
        sensors.handle(new SensorEvent.CarDetected("L1-A-03", t.withHour(9)));

        System.out.println("\n[M5] 09:10 unknown plate -> visitor ticket");
        var s2 = access.enter("999 TU 1111", t.withHour(9).withMinute(10));
        System.out.println("  Ticket " + s2.ticketId() + " (visitor=" + s2.visitor() + ")");
        sensors.handle(new SensorEvent.CarDetected("L1-B-04", t.withHour(9).withMinute(12)));

        System.out.println("\n[M3] Sensor fault on L2-A-05");
        sensors.handle(new SensorEvent.SensorFault("L2-A-05", t.withHour(9).withMinute(30), "no signal"));

        System.out.println("\n[M5+M6] 11:40 visitor leaves -> fee, payment");
        var closed = access.exit("999 TU 1111", t.withHour(11).withMinute(40));
        sensors.handle(new SensorEvent.CarLeft("L1-B-04", closed.exit()));
        System.out.println("  Stay 09:10-11:40, fee = " + closed.fee() + " TND");
        System.out.println("  " + billing.pay(closed.fee(), new PaymentMethod.Cash(10)));

        printMap();
        System.out.printf("%n[M7] Occupancy now: %.0f %%, forecast at 9h: %.0f %%, revenue: %.2f TND%n",
                AnalyticsService.occupancyRate(lot) * 100,
                analytics.forecast(9).orElse(0) * 100,
                billing.totalRevenue());
    }

    void menu() {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("""

                    ==== SmartPark ====
                    1. Live map (free spots)
                    2. Run demo scenario
                    0. Quit""");
            System.out.print("> ");
            if (!in.hasNextLine()) return;
            switch (in.nextLine().trim()) {
                case "1" -> printMap();
                case "2" -> demo();
                case "0" -> { return; }
                default -> System.out.println("Unknown choice");
            }
        }
    }

    private void printMap() {
        System.out.println("\nLive map:");
        lot.allSpots().forEach(s -> System.out.println("  " + s));
        System.out.printf("FREE %d | RESERVED %d | OCCUPIED %d | OUT_OF_SERVICE %d%n",
                lot.count(SpotStatus.FREE), lot.count(SpotStatus.RESERVED),
                lot.count(SpotStatus.OCCUPIED), lot.count(SpotStatus.OUT_OF_SERVICE));
    }
}
