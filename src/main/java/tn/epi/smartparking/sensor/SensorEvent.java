package tn.epi.smartparking.sensor;

import java.time.LocalDateTime;

/**
 * M3 - What a spot sensor can report (F3.1, F3.3).
 * Sealed: the compiler knows these are the only 3 kinds, so a switch on them needs no default.
 */
public sealed interface SensorEvent {

    String spotId();
    LocalDateTime at();

    record CarDetected(String spotId, LocalDateTime at) implements SensorEvent {}
    record CarLeft(String spotId, LocalDateTime at) implements SensorEvent {}
    record SensorFault(String spotId, LocalDateTime at, String reason) implements SensorEvent {}
}
