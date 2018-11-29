package uk.gov.justice.services.event.sourcing.subscription.startup;

import uk.gov.justice.services.subscription.SubscriptionManager;

import java.util.Objects;

public class StartSubscriptionTask implements Runnable {

    private final SubscriptionManager subscriptionManager;

    public StartSubscriptionTask(final SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void run() {
        subscriptionManager.startSubscription();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StartSubscriptionTask that = (StartSubscriptionTask) o;
        return Objects.equals(subscriptionManager, that.subscriptionManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionManager);
    }
}
