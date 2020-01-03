package uk.gov.justice.services.eventstore.management.catchup.state;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.util.Objects;
import java.util.UUID;

public class CatchupError {

    private final String message;
    private final String subscriptionName;
    private final CatchupCommand catchupCommand;
    private final Throwable exception;

    public CatchupError(
            final String message,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final Throwable exception) {
        this.message = message;
        this.subscriptionName = subscriptionName;
        this.catchupCommand = catchupCommand;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
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
        return Objects.equals(message, that.message) &&
                Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, subscriptionName, catchupCommand, exception);
    }

    @Override
    public String toString() {
        return "CatchupError{" +
                "message=" + message +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", catchupCommand=" + catchupCommand +
                ", exception=" + exception +
                '}';
    }
}
