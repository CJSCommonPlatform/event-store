package uk.gov.justice.services.eventstore.management.rebuild.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RebuildStartedEvent {

    private final SystemCommand systemCommand;
    private final ZonedDateTime rebuildStartedAt;

    public RebuildStartedEvent(final SystemCommand systemCommand, final ZonedDateTime rebuildStartedAt) {
        this.systemCommand = systemCommand;
        this.rebuildStartedAt = rebuildStartedAt;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public ZonedDateTime getRebuildStartedAt() {
        return rebuildStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RebuildStartedEvent)) return false;
        final RebuildStartedEvent that = (RebuildStartedEvent) o;
        return Objects.equals(systemCommand, that.systemCommand) &&
                Objects.equals(rebuildStartedAt, that.rebuildStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemCommand, rebuildStartedAt);
    }

    @Override
    public String toString() {
        return "RebuildStartedEvent{" +
                "systemCommand=" + systemCommand +
                ", rebuildStartedAt=" + rebuildStartedAt +
                '}';
    }
}
