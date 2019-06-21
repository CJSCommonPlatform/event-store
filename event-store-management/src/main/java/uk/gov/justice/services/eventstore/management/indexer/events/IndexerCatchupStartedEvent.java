package uk.gov.justice.services.eventstore.management.indexer.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupStartedEvent {

    private final ZonedDateTime catchupStartedAt;

    public IndexerCatchupStartedEvent(final ZonedDateTime catchupStartedAt) {
        this.catchupStartedAt = catchupStartedAt;
    }

    public ZonedDateTime getCatchupStartedAt() {
        return catchupStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupStartedEvent)) return false;
        final IndexerCatchupStartedEvent that = (IndexerCatchupStartedEvent) o;
        return Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedEvent{" +
                "catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
