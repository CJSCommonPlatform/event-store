package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupInProgress {

    private final SubscriptionCatchupDetails subscriptionCatchupDetails;
    private final ZonedDateTime startedAt;

    public CatchupInProgress(final SubscriptionCatchupDetails subscriptionCatchupDetails, final ZonedDateTime startedAt) {
        this.subscriptionCatchupDetails = subscriptionCatchupDetails;
        this.startedAt = startedAt;
    }

    public SubscriptionCatchupDetails getSubscriptionCatchupDetails() {
        return subscriptionCatchupDetails;
    }

    public ZonedDateTime getStartedAt() {
        return startedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupInProgress)) return false;
        final CatchupInProgress that = (CatchupInProgress) o;
        return Objects.equals(subscriptionCatchupDetails, that.subscriptionCatchupDetails) &&
                Objects.equals(startedAt, that.startedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionCatchupDetails, startedAt);
    }

    @Override
    public String toString() {
        return "CatchupInProgress{" +
                "catchupFor=" + subscriptionCatchupDetails +
                ", startedAt=" + startedAt +
                '}';
    }
}
