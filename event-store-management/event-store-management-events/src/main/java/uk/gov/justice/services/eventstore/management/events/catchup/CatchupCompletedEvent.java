package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupCompletedEvent {

    private final UUID commandId;
    private final CatchupCommand catchupCommand;
    private final ZonedDateTime completedAt;

    public CatchupCompletedEvent(final UUID commandId, final CatchupCommand catchupCommand, final ZonedDateTime completedAt) {
        this.commandId = commandId;
        this.catchupCommand = catchupCommand;
        this.completedAt = completedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupCompletedEvent)) return false;
        final CatchupCompletedEvent that = (CatchupCompletedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, catchupCommand, completedAt);
    }

    @Override
    public String
    toString() {
        return "CatchupCompletedEvent{" +
                "commandId=" + commandId +
                ", catchupCommand=" + catchupCommand +
                ", completedAt=" + completedAt +
                '}';
    }
}
