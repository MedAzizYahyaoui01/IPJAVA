# SmartPark — Intelligent Parking System

Java 21 project for the *Project evaluation* of the Java course (École Polytechnique Internationale, IRM).

SmartPark manages a car park in real time: live map of free spots (sensors), reservations,
automatic entry/exit by licence plate, fee calculation with peak hours, payment, occupancy
statistics and an availability forecast.

## Run it

Requirements: JDK 21+, Maven 3.9+.

```bash
mvn test                                        # run the 12 unit tests
mvn compile exec:java -Dexec.args="--demo"      # scripted scenario (workflows 6.1 and 6.2)
mvn compile exec:java                           # interactive menu
```

Without Maven:

```bash
javac -d out $(find src/main -name "*.java")
java -cp out tn.epi.smartparking.App --demo
```

## Modules

| # | Module | Package | Main classes |
|---|--------|---------|--------------|
| M1 | Users & authentication | `auth` | `User` (record), `Role`, `UserService` |
| M2 | Parking infrastructure | `parking` | `ParkingLot`, `ParkingSpot`, `SpotType`, `SpotStatus` |
| M3 | Sensors & monitoring | `sensor` | `SensorEvent` (sealed), `SensorService` |
| M4 | Reservation | `reservation` | `Reservation` (record), `ReservationService` |
| M5 | Access control | `access` | `ParkingSession` (sealed), `AccessService` |
| M6 | Billing & payment | `billing` | `Tariff` (record), `PaymentMethod` (sealed), `BillingService` |
| M7 | Analytics & prediction | `analytics` | `AnalyticsService` |
| M8 | Notifications | `notification` | `NotificationService` |

## Actors

Human: **Visitor**, **Driver**, **Parking agent**, **Administrator**.
System: **Spot sensor**, **Plate camera (ANPR)**, **Payment gateway**, **Scheduler**.

## Java 21 features used

- **Records** — `User`, `Reservation`, `Tariff`, `SensorEvent.*`, `ParkingSession.*`
- **Sealed interfaces** — `SensorEvent`, `ParkingSession`, `PaymentMethod`
- **Pattern matching for `switch` + record patterns** — `SensorService.handle`, `BillingService.pay`
- **Guarded patterns (`when`)** — refusing a cash payment that is too small
- **Text blocks, `String.formatted`, `Stream.toList()`**

## Business rules

- A spot is in one of 4 states: `FREE`, `RESERVED`, `OCCUPIED`, `OUT_OF_SERVICE`.
- A reservation holds the spot 15 minutes after its start time, then expires.
- Fee: 1.50 TND per started hour, ×1.5 during peaks (08:00–10:00, 17:00–19:00),
  first 15 minutes free, capped at 15 TND per day.
- Forecast at hour *h* = average occupancy of past snapshots taken at hour *h*.

## Git workflow

- `main` stable · `develop` integration · `feature/<module>` per module
- One issue per functionality (F1.1 … F8.2), labelled by module
- Milestones: Sprint 1 (M1–M3), Sprint 2 (M4–M6), Sprint 3 (M7–M8)

## Roadmap

- [ ] Replace in-memory storage with JDBC (MySQL/H2)
- [ ] Sensor simulator thread (or real ESP32 sensors over MQTT)
- [ ] JavaFX dashboard or REST API (Spring Boot)
- [ ] Monthly subscriptions (F6.4) and revenue report per period (F7.2)

## Author

Mohamed Aziz Yahyaoui — [GitHub](https://github.com/MedAzizYahyaoui01)
