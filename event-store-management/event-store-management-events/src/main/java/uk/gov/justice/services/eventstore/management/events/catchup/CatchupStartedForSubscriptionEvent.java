package uk.gov.justice.services.eventstore.management.events.catchup;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupStartedForSubscriptionEvent {

    private final UUID commandId;
    private final String subscriptionName;
    private final CatchupType catchupType;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedForSubscriptionEvent(
            final UUID commandId,
            final String subscriptionName,
            final CatchupType catchupType,
            final ZonedDateTime catchupStartedAt) {
        this.commandId = commandId;
        this.subscriptionName = subscriptionName;
        this.catchupType = catchupType;
        this.catchupStartedAt = catchupStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public CatchupType getCatchupType() {
        return catchupType;
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
                catchupType == that.catchupType &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, subscriptionName, catchupType, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedForSubscriptionEvent{" +
                "commandId=" + commandId +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", catchupType=" + catchupType +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
