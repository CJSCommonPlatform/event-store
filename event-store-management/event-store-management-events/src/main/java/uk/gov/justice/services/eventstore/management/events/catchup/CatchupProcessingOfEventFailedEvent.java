package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.util.Objects;
import java.util.UUID;

public class CatchupProcessingOfEventFailedEvent {

    private final UUID commandId;
    private final String message;
    private final Throwable exception;
    private final CatchupCommand catchupCommand;
    private final String subscriptionName;

    public CatchupProcessingOfEventFailedEvent(
            final UUID commandId,
            final String message,
            final Throwable exception,
            final CatchupCommand catchupCommand,
            final String subscriptionName) {
        this.commandId = commandId;
        this.message = message;
        this.exception = exception;
        this.catchupCommand = catchupCommand;
        this.subscriptionName = subscriptionName;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }

    public CatchupCommand getCatchupCommand() {
        return catchupCommand;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupProcessingOfEventFailedEvent)) return false;
        final CatchupProcessingOfEventFailedEvent that = (CatchupProcessingOfEventFailedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(exception, that.exception) &&
                Objects.equals(catchupCommand, that.catchupCommand) &&
                Objects.equals(subscriptionName, that.subscriptionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, message, exception, catchupCommand, subscriptionName);
    }

    @Override
    public String toString() {
        return "CatchupProcessingOfEventFailedEvent{" +
                "commandId=" + commandId +
                ", message=" + message +
                ", exception=" + exception +
                ", catchupCommand=" + catchupCommand +
                ", subscriptionName='" + subscriptionName + '\'' +
                '}';
    }
}
