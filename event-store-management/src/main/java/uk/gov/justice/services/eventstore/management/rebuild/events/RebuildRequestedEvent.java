package uk.gov.justice.services.eventstore.management.rebuild.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RebuildRequestedEvent {

    private final SystemCommand target;
    private final ZonedDateTime rebuildRequestedAt;

    public RebuildRequestedEvent(final ZonedDateTime rebuildRequestedAt, final SystemCommand target) {
        this.target = target;
        this.rebuildRequestedAt = rebuildRequestedAt;
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
        return Objects.equals(target, that.target) &&
                Objects.equals(rebuildRequestedAt, that.rebuildRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, rebuildRequestedAt);
    }

    @Override
    public String toString() {
        return "RebuildRequestedEvent{" +
                "target=" + target +
                ", rebuildRequestedAt=" + rebuildRequestedAt +
                '}';
    }
}
