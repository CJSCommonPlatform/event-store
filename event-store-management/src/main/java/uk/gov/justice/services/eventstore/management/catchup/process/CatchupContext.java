package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;

public class CatchupContext {

    private final String componentName;
    private final Subscription subscription;
    private final CatchupRequestedEvent catchupRequestedEvent;

    public CatchupContext(final String componentName, final Subscription subscription, final CatchupRequestedEvent catchupRequestedEvent) {
        this.componentName = componentName;
        this.subscription = subscription;
        this.catchupRequestedEvent = catchupRequestedEvent;
    }

    public String getComponentName() {
        return componentName;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public CatchupRequestedEvent getCatchupRequestedEvent() {
        return catchupRequestedEvent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupContext)) return false;
        final CatchupContext that = (CatchupContext) o;
        return Objects.equals(componentName, that.componentName) &&
                Objects.equals(subscription, that.subscription) &&
                Objects.equals(catchupRequestedEvent, that.catchupRequestedEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentName, subscription, catchupRequestedEvent);
    }

    @Override
    public String toString() {
        return "CatchupContext{" +
                "componentName='" + componentName + '\'' +
                ", subscription=" + subscription +
                ", catchupRequestedEvent=" + catchupRequestedEvent +
                '}';
    }
}
