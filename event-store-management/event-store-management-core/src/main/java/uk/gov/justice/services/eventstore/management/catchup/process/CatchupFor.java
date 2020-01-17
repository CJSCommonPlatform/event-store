package uk.gov.justice.services.eventstore.management.catchup.process;

import java.util.Objects;

public class CatchupFor {

    private final String subscriptionName;
    private final String componentName;

    public CatchupFor(final String subscriptionName, final String componentName) {
        this.subscriptionName = subscriptionName;
        this.componentName = componentName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public String getComponentName() {
        return componentName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupFor)) return false;
        final CatchupFor that = (CatchupFor) o;
        return Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName, componentName);
    }

    @Override
    public String toString() {
        return "CatchupFor{" +
                "subscriptionName='" + subscriptionName + '\'' +
                ", componentName='" + componentName + '\'' +
                '}';
    }
}
