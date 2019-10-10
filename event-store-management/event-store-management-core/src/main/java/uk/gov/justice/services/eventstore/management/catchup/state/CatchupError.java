package uk.gov.justice.services.eventstore.management.catchup.state;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.util.Objects;
import java.util.UUID;

public class CatchupError {

    private final UUID eventId;
    private final String metadata;
    private final String subscriptionName;
    private final CatchupCommand catchupCommand;
    private final Throwable exception;

    public CatchupError(
            final UUID eventId,
            final String metadata,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final Throwable exception) {
        this.eventId = eventId;
        this.metadata = metadata;
        this.subscriptionName = subscriptionName;
        this.catchupCommand = catchupCommand;
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

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
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
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, metadata, subscriptionName, catchupCommand, exception);
    }

    @Override
    public String toString() {
        return "CatchupError{" +
                "eventId=" + eventId +
                ", metadata='" + metadata + '\'' +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", catchupCommand=" + catchupCommand +
                ", exception=" + exception +
                '}';
    }
}
