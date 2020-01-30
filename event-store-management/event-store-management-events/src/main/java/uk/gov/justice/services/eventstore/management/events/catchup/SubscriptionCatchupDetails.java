package uk.gov.justice.services.eventstore.management.events.catchup;

import java.util.Objects;

public class SubscriptionCatchupDetails {

    private final String subscriptionName;
    private final String eventSourceName;
    private final String componentName;

    public SubscriptionCatchupDetails(final String subscriptionName,
                                      final String eventSourceName,
                                      final String componentName) {
        this.subscriptionName = subscriptionName;
        this.eventSourceName = eventSourceName;
        this.componentName = componentName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public String getComponentName() {
        return componentName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionCatchupDetails)) return false;
        final SubscriptionCatchupDetails that = (SubscriptionCatchupDetails) o;
        return Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(eventSourceName, that.eventSourceName) &&
                Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName, eventSourceName, componentName);
    }

    @Override
    public String toString() {
        return "CatchupFor{" +
                "subscriptionName='" + subscriptionName + '\'' +
                ", eventSourceName='" + eventSourceName + '\'' +
                ", componentName='" + componentName + '\'' +
                '}';
    }
}
