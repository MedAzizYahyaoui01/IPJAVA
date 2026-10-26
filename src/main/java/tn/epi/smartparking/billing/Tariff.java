package tn.epi.smartparking.billing;

/**
 * M2/M6 - Pricing rules (F2.3), in Tunisian dinars (TND).
 *
 * @param hourlyRate    price per started hour
 * @param dailyCap      the fee never goes above this for 24 h
 * @param peakMultiplier applied to hours that start in a peak window
 * @param freeMinutes   stays shorter than this are free
 */
public record Tariff(double hourlyRate, double dailyCap, double peakMultiplier, int freeMinutes) {

    public static Tariff standard() {
        return new Tariff(1.5, 15.0, 1.5, 15);
    }
}
