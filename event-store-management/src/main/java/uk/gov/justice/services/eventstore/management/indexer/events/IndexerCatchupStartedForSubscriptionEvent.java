package uk.gov.justice.services.eventstore.management.indexer.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupStartedForSubscriptionEvent {

    private final String subscriptionName;
    private final ZonedDateTime catchupStartedAt;

    public IndexerCatchupStartedForSubscriptionEvent(final String subscriptionName, final ZonedDateTime catchupStartedAt) {
        this.subscriptionName = subscriptionName;
        this.catchupStartedAt = catchupStartedAt;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public ZonedDateTime getCatchupStartedAt() {
        return catchupStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupStartedForSubscriptionEvent)) return false;
        final IndexerCatchupStartedForSubscriptionEvent that = (IndexerCatchupStartedForSubscriptionEvent) o;
        return Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedForSubscriptionEvent{" +
                "eventSourceName='" + subscriptionName + '\'' +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
