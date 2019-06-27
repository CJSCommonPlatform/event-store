package uk.gov.justice.services.eventstore.management.indexer.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupCompletedEvent {
    private final SystemCommand target;
    private final ZonedDateTime completedAt;

    public IndexerCatchupCompletedEvent(final SystemCommand target, final ZonedDateTime completedAt) {
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
        if (!(o instanceof IndexerCatchupCompletedEvent)) return false;
        final IndexerCatchupCompletedEvent that = (IndexerCatchupCompletedEvent) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, completedAt);
    }

    @Override
    public String toString() {
        return "IndexerCatchupCompletedEvent{" +
                "target=" + target +
                ", completedAt=" + completedAt +
                '}';
    }
}
