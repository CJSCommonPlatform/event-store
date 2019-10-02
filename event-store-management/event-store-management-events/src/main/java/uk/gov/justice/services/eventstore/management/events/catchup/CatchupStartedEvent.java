package uk.gov.justice.services.eventstore.management.events.catchup;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupStartedEvent {

    private final UUID commandId;
    private final CatchupType catchupType;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedEvent(
            final UUID commandId,
            final CatchupType catchupType,
            final ZonedDateTime catchupStartedAt) {
        this.commandId = commandId;
        this.catchupType = catchupType;
        this.catchupStartedAt = catchupStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
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
        if (!(o instanceof CatchupStartedEvent)) return false;
        final CatchupStartedEvent that = (CatchupStartedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                catchupType == that.catchupType &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, catchupType, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedEvent{" +
                "commandId=" + commandId +
                ", catchupType=" + catchupType +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
