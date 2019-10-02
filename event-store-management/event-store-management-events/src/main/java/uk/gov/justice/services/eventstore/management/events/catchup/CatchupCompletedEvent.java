package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupCompletedEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime completedAt;
    private final CatchupType catchupType;

    public CatchupCompletedEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime completedAt,
            final CatchupType catchupType) {
        this.commandId = commandId;
        this.target = target;
        this.completedAt = completedAt;
        this.catchupType = catchupType;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    public CatchupType getCatchupType() {
        return catchupType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupCompletedEvent)) return false;
        final CatchupCompletedEvent that = (CatchupCompletedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(completedAt, that.completedAt) &&
                catchupType == that.catchupType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, completedAt, catchupType);
    }

    @Override
    public String toString() {
        return "CatchupCompletedEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", completedAt=" + completedAt +
                ", catchupType=" + catchupType +
                '}';
    }
}
