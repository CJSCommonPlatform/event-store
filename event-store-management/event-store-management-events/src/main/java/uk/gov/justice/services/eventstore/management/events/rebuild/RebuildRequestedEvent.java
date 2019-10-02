package uk.gov.justice.services.eventstore.management.events.rebuild;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class RebuildRequestedEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime rebuildRequestedAt;

    public RebuildRequestedEvent(
            final UUID commandId,
            final ZonedDateTime rebuildRequestedAt,
            final SystemCommand target) {
        this.commandId = commandId;
        this.target = target;
        this.rebuildRequestedAt = rebuildRequestedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getRebuildRequestedAt() {
        return rebuildRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RebuildRequestedEvent)) return false;
        final RebuildRequestedEvent that = (RebuildRequestedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(rebuildRequestedAt, that.rebuildRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, rebuildRequestedAt);
    }

    @Override
    public String toString() {
        return "RebuildRequestedEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", rebuildRequestedAt=" + rebuildRequestedAt +
                '}';
    }
}
