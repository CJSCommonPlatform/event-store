package uk.gov.justice.services.eventstore.management.catchup.state;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.Objects;
import java.util.UUID;

public class CatchupError {

    private final UUID eventId;
    private final String metadata;
    private final String subscriptionName;
    private final CatchupType catchupType;
    private final Throwable exception;

    public CatchupError(
            final UUID eventId,
            final String metadata,
            final String subscriptionName,
            final CatchupType catchupType, final Throwable exception) {
        this.eventId = eventId;
        this.metadata = metadata;
        this.subscriptionName = subscriptionName;
        this.catchupType = catchupType;
        this.exception = exception;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public CatchupType getCatchupType() {
        return catchupType;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupError)) return false;
        final CatchupError that = (CatchupError) o;
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(subscriptionName, that.subscriptionName) &&
                catchupType == that.catchupType &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, metadata, subscriptionName, catchupType, exception);
    }

    @Override
    public String toString() {
        return "CatchupError{" +
                "eventId=" + eventId +
                ", metadata='" + metadata + '\'' +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", catchupType=" + catchupType +
                ", exception=" + exception +
                '}';
    }
}
