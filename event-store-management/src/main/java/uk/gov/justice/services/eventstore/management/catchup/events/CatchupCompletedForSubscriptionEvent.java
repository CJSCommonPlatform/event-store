package uk.gov.justice.services.eventstore.management.catchup.events;

import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupCompletedForSubscriptionEvent {

    private final CatchupType catchupType;
    private final String subscriptionName;
    private final String eventSourceName;
    private final String componentName;
    private final SystemCommand target;
    private final ZonedDateTime catchupCompletedAt;
    private final int totalNumberOfEvents;

    public CatchupCompletedForSubscriptionEvent(
            final CatchupType catchupType, final String subscriptionName,
            final String eventSourceName,
            final String componentName,
            final SystemCommand target,
            final ZonedDateTime catchupCompletedAt,
            final int totalNumberOfEvents) {
        this.catchupType = catchupType;
        this.subscriptionName = subscriptionName;
        this.eventSourceName = eventSourceName;
        this.componentName = componentName;
        this.target = target;
        this.catchupCompletedAt = catchupCompletedAt;
        this.totalNumberOfEvents = totalNumberOfEvents;
    }

    public CatchupType getCatchupType() {
        return catchupType;
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

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getCatchupCompletedAt() {
        return catchupCompletedAt;
    }

    public int getTotalNumberOfEvents() {
        return totalNumberOfEvents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupCompletedForSubscriptionEvent)) return false;
        final CatchupCompletedForSubscriptionEvent that = (CatchupCompletedForSubscriptionEvent) o;
        return totalNumberOfEvents == that.totalNumberOfEvents &&
                catchupType == that.catchupType &&
                Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(eventSourceName, that.eventSourceName) &&
                Objects.equals(componentName, that.componentName) &&
                Objects.equals(target, that.target) &&
                Objects.equals(catchupCompletedAt, that.catchupCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchupType, subscriptionName, eventSourceName, componentName, target, catchupCompletedAt, totalNumberOfEvents);
    }

    @Override
    public String toString() {
        return "CatchupCompletedForSubscriptionEvent{" +
                "catchupType=" + catchupType +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", eventSourceName='" + eventSourceName + '\'' +
                ", componentName='" + componentName + '\'' +
                ", target=" + target +
                ", catchupCompletedAt=" + catchupCompletedAt +
                ", totalNumberOfEvents=" + totalNumberOfEvents +
                '}';
    }
}
