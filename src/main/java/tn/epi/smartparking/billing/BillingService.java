package tn.epi.smartparking.billing;

import tn.epi.smartparking.billing.PaymentMethod.Card;
import tn.epi.smartparking.billing.PaymentMethod.Cash;
import tn.epi.smartparking.billing.PaymentMethod.Wallet;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** M6 - Fee calculation and payment (F6.1, F6.2, F6.3). */
public class BillingService {

    /** Peak windows: 08:00-10:00 and 17:00-19:00. */
    private static final LocalTime[][] PEAKS = {
            {LocalTime.of(8, 0), LocalTime.of(10, 0)},
            {LocalTime.of(17, 0), LocalTime.of(19, 0)}
    };

    private final Tariff tariff;
    private double revenue;

    public BillingService(Tariff tariff) {
        this.tariff = tariff;
    }

    /** F6.1 - Each started hour is charged; hours starting in a peak window cost more. */
    public double computeFee(LocalDateTime entry, LocalDateTime exit) {
        long minutes = Duration.between(entry, exit).toMinutes();
        if (minutes < tariff.freeMinutes()) {
            return 0.0;
        }
        long hours = (minutes + 59) / 60;                   // started hours
        double fee = 0;
        for (int h = 0; h < hours; h++) {
            LocalTime hourStart = entry.plusHours(h).toLocalTime();
            fee += tariff.hourlyRate() * (isPeak(hourStart) ? tariff.peakMultiplier() : 1.0);
        }
        long days = hours / 24;
        double cap = tariff.dailyCap() * (days + 1);
        return round(Math.min(fee, cap));
    }

    /** F6.2 - Returns a receipt line. Pattern matching on the sealed PaymentMethod. */
    public String pay(double amount, PaymentMethod method) {
        String detail = switch (method) {
            case Card(String last4) -> "card ****" + last4;
            case Cash(double given) when given < amount ->
                    throw new IllegalArgumentException("Not enough cash: " + given + " < " + amount);
            case Cash(double given) -> "cash, change " + round(given - amount) + " TND";
            case Wallet(String email) -> "wallet of " + email;
        };
        revenue += amount;
        return "Paid %.2f TND by %s".formatted(amount, detail);       // F6.3 invoice line
    }

    public double totalRevenue() {                                     // F7.2
        return round(revenue);
    }

    static boolean isPeak(LocalTime t) {
        for (LocalTime[] p : PEAKS) {
            if (!t.isBefore(p[0]) && t.isBefore(p[1])) return true;
        }
        return false;
    }

    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
