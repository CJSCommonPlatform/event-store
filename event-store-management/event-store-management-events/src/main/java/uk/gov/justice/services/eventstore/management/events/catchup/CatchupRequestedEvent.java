package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupRequestedEvent {

    private final UUID commandId;
    private final CatchupType catchupType;
    private final SystemCommand target;
    private final ZonedDateTime catchupRequestedAt;

    public CatchupRequestedEvent(
            final UUID commandId,
            final CatchupType catchupType,
            final SystemCommand target,
            final ZonedDateTime catchupRequestedAt) {
        this.commandId = commandId;
        this.catchupType = catchupType;
        this.target = target;
        this.catchupRequestedAt = catchupRequestedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public CatchupType getCatchupType() {
        return catchupType;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getCatchupRequestedAt() {
        return catchupRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupRequestedEvent)) return false;
        final CatchupRequestedEvent that = (CatchupRequestedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                catchupType == that.catchupType &&
                Objects.equals(target, that.target) &&
                Objects.equals(catchupRequestedAt, that.catchupRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, catchupType, target, catchupRequestedAt);
    }

    @Override
    public String toString() {
        return "CatchupRequestedEvent{" +
                "commandId=" + commandId +
                ", catchupType=" + catchupType +
                ", target=" + target +
                ", catchupRequestedAt=" + catchupRequestedAt +
                '}';
    }
}
