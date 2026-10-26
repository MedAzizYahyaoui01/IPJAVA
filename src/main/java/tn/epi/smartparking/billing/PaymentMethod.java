package tn.epi.smartparking.billing;

/** M6 - How a fee can be paid (F6.2). */
public sealed interface PaymentMethod {

    record Card(String last4) implements PaymentMethod {}
    record Cash(double given) implements PaymentMethod {}
    record Wallet(String driverEmail) implements PaymentMethod {}
}
