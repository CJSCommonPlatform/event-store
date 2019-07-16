package uk.gov.justice.services.eventstore.management.catchup.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupCompletedEvent {

    private final SystemCommand target;
    private final ZonedDateTime completedAt;

    public CatchupCompletedEvent(final SystemCommand target, final ZonedDateTime completedAt) {
        this.target = target;
        this.completedAt = completedAt;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    public SystemCommand getTarget() {
        return target;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupCompletedEvent)) return false;
        final CatchupCompletedEvent that = (CatchupCompletedEvent) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, completedAt);
    }

    @Override
    public String toString() {
        return "CatchupCompletedEvent{" +
                "target=" + target +
                ", completedAt=" + completedAt +
                '}';
    }
}
