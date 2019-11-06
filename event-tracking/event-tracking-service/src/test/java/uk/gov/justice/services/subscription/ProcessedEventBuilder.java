package uk.gov.justice.services.subscription;

import java.util.UUID;

public class ProcessedEventBuilder {

    private UUID eventId;
    private long previousEventNumber;
    private long eventNumber;
    private String source;
    private String componentName;

    private ProcessedEventBuilder() {}

    public static ProcessedEventBuilder processedEventTrackItem() {
        return new ProcessedEventBuilder();
    }

    public ProcessedEventBuilder withEventId(final UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    public ProcessedEventBuilder withPreviousEventNumber(final long previousEventNumber) {
        this.previousEventNumber = previousEventNumber;
        return this;
    }

    public ProcessedEventBuilder withEventNumber(final long eventNumber) {
        this.eventNumber = eventNumber;
        return this;
    }

    public ProcessedEventBuilder withSource(final String source) {
        this.source = source;
        return this;
    }

    public ProcessedEventBuilder withComponentName(final String componentName) {
        this.componentName = componentName;
        return this;
    }

    public ProcessedEvent build() {
        return new ProcessedEvent(eventId, previousEventNumber, eventNumber, source, componentName);
    }
}
