package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CatchupStartedEvent {

    private final UUID commandId;
    private final CatchupCommand catchupCommand;
    private final List<SubscriptionCatchupDetails> subscriptionCatchupDefinition;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedEvent(
            final UUID commandId,
            final CatchupCommand catchupCommand,
            final List<SubscriptionCatchupDetails> subscriptionCatchupDefinition,
            final ZonedDateTime catchupStartedAt) {
        this.commandId = commandId;
        this.catchupCommand = catchupCommand;
        this.subscriptionCatchupDefinition = subscriptionCatchupDefinition;
        this.catchupStartedAt = catchupStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
    }

    public List<SubscriptionCatchupDetails> getSubscriptionCatchupDefinition() {
        return subscriptionCatchupDefinition;
    }

    public ZonedDateTime getCatchupStartedAt() {
        return catchupStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupStartedEvent)) return false;
        final CatchupStartedEvent that = (CatchupStartedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(subscriptionCatchupDefinition, that.subscriptionCatchupDefinition) &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, catchupCommand, subscriptionCatchupDefinition, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedEvent{" +
                "commandId=" + commandId +
                ", catchupCommand=" + catchupCommand +
                ", subscriptionCatchupDefinition=" + subscriptionCatchupDefinition +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
