package uk.gov.justice.services.eventstore.management.catchup.process;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupInProgress {

    private final String subscriptionName;
    private final ZonedDateTime startedAt;

    public CatchupInProgress(final String subscriptionName, final ZonedDateTime startedAt) {
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
        if (!(o instanceof CatchupInProgress)) return false;
        final CatchupInProgress that = (CatchupInProgress) o;
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
