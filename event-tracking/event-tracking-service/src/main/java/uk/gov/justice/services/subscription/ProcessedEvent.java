package uk.gov.justice.services.subscription;

import java.util.Objects;
import java.util.UUID;

public class ProcessedEvent {

    private final UUID eventId;
    private final long previousEventNumber;
    private final long eventNumber;
    private final String source;
    private final String componentName;

    public ProcessedEvent(
            final UUID eventId,
            final long previousEventNumber,
            final long eventNumber,
            final String source,
            final String componentName) {
        this.eventId = eventId;
        this.previousEventNumber = previousEventNumber;
        this.eventNumber = eventNumber;
        this.source = source;
        this.componentName = componentName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public long getPreviousEventNumber() {
        return previousEventNumber;
    }

    public long getEventNumber() {
        return eventNumber;
    }

    public String getSource() {
        return source;
    }

    public String getComponentName() {
        return componentName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessedEvent)) return false;
        final ProcessedEvent that = (ProcessedEvent) o;
        return previousEventNumber == that.previousEventNumber &&
                eventNumber == that.eventNumber &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(source, that.source) &&
                Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, previousEventNumber, eventNumber, source, componentName);
    }

    @Override
    public String toString() {
        return "ProcessedEvent{" +
                "eventId=" + eventId +
                ", previousEventNumber=" + previousEventNumber +
                ", eventNumber=" + eventNumber +
                ", source='" + source + '\'' +
                ", componentName='" + componentName + '\'' +
                '}';
    }
}
