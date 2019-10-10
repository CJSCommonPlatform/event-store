package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class CatchupStartedEvent {

    private final UUID commandId;
    private final CatchupCommand catchupCommand;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedEvent(final UUID commandId, final CatchupCommand catchupCommand, final ZonedDateTime catchupStartedAt) {
        this.commandId = commandId;
        this.catchupCommand = catchupCommand;
        this.catchupStartedAt = catchupStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
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
        if (!(o instanceof CatchupStartedEvent)) return false;
        final CatchupStartedEvent that = (CatchupStartedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, catchupCommand, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedEvent{" +
                "commandId=" + commandId +
                ", catchupCommand=" + catchupCommand +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
