package uk.gov.justice.services.eventstore.management.rebuild.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RebuildCompleteEvent {

    private final SystemCommand target;
    private final ZonedDateTime rebuildCompletedAt;

    public RebuildCompleteEvent(final SystemCommand target, final ZonedDateTime rebuildCompletedAt) {
        this.target = target;
        this.rebuildCompletedAt = rebuildCompletedAt;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getRebuildCompletedAt() {
        return rebuildCompletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RebuildCompleteEvent)) return false;
        final RebuildCompleteEvent that = (RebuildCompleteEvent) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(rebuildCompletedAt, that.rebuildCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, rebuildCompletedAt);
    }

    @Override
    public String toString() {
        return "RebuildCompleteEvent{" +
                "target=" + target +
                ", rebuildStartedAt=" + rebuildCompletedAt +
                '}';
    }
}
