package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupStartedForSubscriptionEvent {

    private final UUID commandId;
    private final String subscriptionName;
    private final CatchupCommand catchupCommand;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedForSubscriptionEvent(
            final UUID commandId,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final ZonedDateTime catchupStartedAt) {
        this.commandId = commandId;
        this.subscriptionName = subscriptionName;
        this.catchupCommand = catchupCommand;
        this.catchupStartedAt = catchupStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
    }

    public ZonedDateTime getCatchupStartedAt() {
        return catchupStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupStartedForSubscriptionEvent)) return false;
        final CatchupStartedForSubscriptionEvent that = (CatchupStartedForSubscriptionEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, subscriptionName, catchupCommand, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedForSubscriptionEvent{" +
                "commandId=" + commandId +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", catchupCommand=" + catchupCommand +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
