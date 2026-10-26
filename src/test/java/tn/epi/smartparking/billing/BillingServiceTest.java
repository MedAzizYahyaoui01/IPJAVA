package tn.epi.smartparking.billing;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private final BillingService billing = new BillingService(Tariff.standard());
    private final LocalDateTime day = LocalDateTime.of(2026, 10, 26, 0, 0);

    @Test
    void shortStayIsFree() {
        assertEquals(0.0, billing.computeFee(day.withHour(12), day.withHour(12).withMinute(10)));
    }

    @Test
    void offPeakHoursUseBaseRate() {
        // 12:00-14:30 = 3 started hours x 1.5
        assertEquals(4.5, billing.computeFee(day.withHour(12), day.withHour(14).withMinute(30)));
    }

    @Test
    void peakHoursCostMore() {
        // 08:00-10:00 = 2 peak hours x 1.5 x 1.5
        assertEquals(4.5, billing.computeFee(day.withHour(8), day.withHour(10)));
    }

    @Test
    void feeIsCappedPerDay() {
        assertEquals(15.0, billing.computeFee(day, day.withHour(23)));
    }

    @Test
    void cashPaymentGivesChange() {
        assertTrue(billing.pay(4.5, new PaymentMethod.Cash(10)).contains("change 5.5"));
    }

    @Test
    void notEnoughCashIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> billing.pay(4.5, new PaymentMethod.Cash(2)));
    }
}
