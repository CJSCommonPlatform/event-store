package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupRequestedEvent {

    private final UUID commandId;
    private final CatchupCommand catchupCommand;
    private final ZonedDateTime catchupRequestedAt;

    public CatchupRequestedEvent(
            final UUID commandId,
            final CatchupCommand catchupCommand,
            final ZonedDateTime catchupRequestedAt) {
        this.commandId = commandId;
        this.catchupCommand = catchupCommand;
        this.catchupRequestedAt = catchupRequestedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
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
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(catchupRequestedAt, that.catchupRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, catchupCommand, catchupRequestedAt);
    }

    @Override
    public String toString() {
        return "CatchupRequestedEvent{" +
                "commandId=" + commandId +
                ", catchupCommand=" + catchupCommand +
                ", catchupRequestedAt=" + catchupRequestedAt +
                '}';
    }
}
