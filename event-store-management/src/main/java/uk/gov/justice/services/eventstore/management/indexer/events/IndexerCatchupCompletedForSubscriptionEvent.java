package uk.gov.justice.services.eventstore.management.indexer.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupCompletedForSubscriptionEvent {

    private final String eventSourceName;
    private final ZonedDateTime catchupCompletedAt;
    private final int totalNumberOfEvents;

    public IndexerCatchupCompletedForSubscriptionEvent(
            final String eventSourceName,
            final int totalNumberOfEvents,
            final ZonedDateTime catchupCompletedAt) {

        this.eventSourceName = eventSourceName;
        this.catchupCompletedAt = catchupCompletedAt;
        this.totalNumberOfEvents = totalNumberOfEvents;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public ZonedDateTime getCatchupCompletedAt() {
        return catchupCompletedAt;
    }

    public int getTotalNumberOfEvents() {
        return totalNumberOfEvents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupCompletedForSubscriptionEvent)) return false;
        final IndexerCatchupCompletedForSubscriptionEvent that = (IndexerCatchupCompletedForSubscriptionEvent) o;
        return totalNumberOfEvents == that.totalNumberOfEvents &&
                Objects.equals(eventSourceName, that.eventSourceName) &&
                Objects.equals(catchupCompletedAt, that.catchupCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventSourceName, catchupCompletedAt, totalNumberOfEvents);
    }

    @Override
    public String toString() {
        return "IndexerCatchupCompletedForSubscriptionEvent{" +
                "eventSourceName='" + eventSourceName + '\'' +
                ", catchupCompletedAt=" + catchupCompletedAt +
                ", totalNumberOfEvents=" + totalNumberOfEvents +
                '}';
    }
}
