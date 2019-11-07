package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupCompletedForSubscriptionEvent {

    private final UUID commandId;
    private final String subscriptionName;
    private final String eventSourceName;
    private final String componentName;
    private final CatchupCommand catchupCommand;
    private final ZonedDateTime catchupCompletedAt;
    private final int totalNumberOfEvents;

    public CatchupCompletedForSubscriptionEvent(
            final UUID commandId,
            final String subscriptionName,
            final String eventSourceName,
            final String componentName,
            final CatchupCommand catchupCommand,
            final ZonedDateTime catchupCompletedAt,
            final int totalNumberOfEvents) {
        this.commandId = commandId;
        this.subscriptionName = subscriptionName;
        this.eventSourceName = eventSourceName;
        this.componentName = componentName;
        this.catchupCommand = catchupCommand;
        this.catchupCompletedAt = catchupCompletedAt;
        this.totalNumberOfEvents = totalNumberOfEvents;
    }

    public UUID getCommandId() {
        return commandId;
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

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
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
                Objects.equals(commandId, that.commandId) &&
                Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(eventSourceName, that.eventSourceName) &&
                Objects.equals(componentName, that.componentName) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(catchupCompletedAt, that.catchupCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, subscriptionName, eventSourceName, componentName, catchupCommand, catchupCompletedAt, totalNumberOfEvents);
    }

    @Override
    public String toString() {
        return "CatchupCompletedForSubscriptionEvent{" +
                "commandId=" + commandId +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", eventSourceName='" + eventSourceName + '\'' +
                ", componentName='" + componentName + '\'' +
                ", catchupCommand=" + catchupCommand +
                ", catchupCompletedAt=" + catchupCompletedAt +
                ", totalNumberOfEvents=" + totalNumberOfEvents +
                '}';
    }
}
