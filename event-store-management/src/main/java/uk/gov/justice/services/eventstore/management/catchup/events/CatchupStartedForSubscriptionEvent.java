package uk.gov.justice.services.eventstore.management.catchup.events;

import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupStartedForSubscriptionEvent {

    private final String subscriptionName;
    private final CatchupType catchupType;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedForSubscriptionEvent(
            final String subscriptionName,
            final CatchupType catchupType,
            final ZonedDateTime catchupStartedAt) {
        this.subscriptionName = subscriptionName;
        this.catchupType = catchupType;
        this.catchupStartedAt = catchupStartedAt;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public CatchupType getCatchupType() {
        return catchupType;
    }

    public ZonedDateTime getCatchupStartedAt() {
        return catchupStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupStartedForSubscriptionEvent)) return false;
        final CatchupStartedForSubscriptionEvent that = (CatchupStartedForSubscriptionEvent) o;
        return Objects.equals(subscriptionName, that.subscriptionName) &&
                catchupType == that.catchupType &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName, catchupType, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedForSubscriptionEvent{" +
                "subscriptionName='" + subscriptionName + '\'' +
                ", catchupType=" + catchupType +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
