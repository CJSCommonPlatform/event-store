package uk.gov.justice.services.eventstore.management.validation.process;

import java.util.Objects;
import java.util.UUID;

public class ValidationError {

    private final String eventName;
    private UUID eventId;
    private final String errorMessage;

    public ValidationError(final String eventName, final UUID eventId, final String errorMessage) {
        this.eventName = eventName;
        this.eventId = eventId;
        this.errorMessage = errorMessage;
    }

    public String getEventName() {
        return eventName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationError)) return false;
        final ValidationError that = (ValidationError) o;
        return Objects.equals(eventName, that.eventName) &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, eventId, errorMessage);
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "eventName='" + eventName + '\'' +
                ", eventId='" + eventId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
