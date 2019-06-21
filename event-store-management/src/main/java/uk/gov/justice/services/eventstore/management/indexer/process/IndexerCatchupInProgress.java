package uk.gov.justice.services.eventstore.management.indexer.process;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupInProgress {

    private final String subscriptionName;
    private final ZonedDateTime startedAt;

    public IndexerCatchupInProgress(final String subscriptionName, final ZonedDateTime startedAt) {
        this.subscriptionName = subscriptionName;
        this.startedAt = startedAt;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public ZonedDateTime getStartedAt() {
        return startedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupInProgress)) return false;
        final IndexerCatchupInProgress that = (IndexerCatchupInProgress) o;
        return Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(startedAt, that.startedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName, startedAt);
    }

    @Override
    public String toString() {
        return "CatchupInProgress{" +
                "subscriptionName='" + subscriptionName + '\'' +
                ", startedAt=" + startedAt +
                '}';
    }
}
