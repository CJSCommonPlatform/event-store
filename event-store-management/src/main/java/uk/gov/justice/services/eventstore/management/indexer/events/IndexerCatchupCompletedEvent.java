package uk.gov.justice.services.eventstore.management.indexer.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupCompletedEvent {

    private final ZonedDateTime completedAt;

    public IndexerCatchupCompletedEvent(final ZonedDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupCompletedEvent)) return false;
        final IndexerCatchupCompletedEvent that = (IndexerCatchupCompletedEvent) o;
        return Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(completedAt);
    }

    @Override
    public String toString() {
        return "IndexerCatchupCompletedEvent{" +
                "completedAt=" + completedAt +
                '}';
    }
}
