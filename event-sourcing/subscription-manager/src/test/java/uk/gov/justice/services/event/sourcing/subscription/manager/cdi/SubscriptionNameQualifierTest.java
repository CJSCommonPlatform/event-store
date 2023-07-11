package uk.gov.justice.services.event.sourcing.subscription.manager.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class SubscriptionNameQualifierTest {

    @Test
    public void shouldReturnFalseForDifferentSubscriptionNameQualifiers() {
        final SubscriptionNameQualifier subscriptionNameQualifier1 = new SubscriptionNameQualifier("ABC");
        final SubscriptionNameQualifier subscriptionNameQualifier2 = new SubscriptionNameQualifier("EFG");
        assertNotEquals(subscriptionNameQualifier1, subscriptionNameQualifier2);
    }

    @Test
    public void shouldReturnTrueForSameSubscriptionNameQualifiers() {
        final SubscriptionNameQualifier subscriptionNameQualifier1 = new SubscriptionNameQualifier("ABC");
        final SubscriptionNameQualifier subscriptionNameQualifier2 = new SubscriptionNameQualifier("ABC");
        assertEquals(subscriptionNameQualifier1, subscriptionNameQualifier2);
    }
}
