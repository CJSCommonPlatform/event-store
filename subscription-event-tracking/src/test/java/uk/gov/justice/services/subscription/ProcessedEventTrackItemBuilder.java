package uk.gov.justice.services.subscription;

public class ProcessedEventTrackItemBuilder {

    private long previousEventNumber;
    private long eventNumber;
    private String source;
    private String componentName;

    private ProcessedEventTrackItemBuilder() {}

    public static ProcessedEventTrackItemBuilder processedEventTrackItem() {
        return new ProcessedEventTrackItemBuilder();
    }

    public ProcessedEventTrackItemBuilder withPreviousEventNumber(final long previousEventNumber) {
        this.previousEventNumber = previousEventNumber;
        return this;
    }

    public ProcessedEventTrackItemBuilder withEventNumber(final long eventNumber) {
        this.eventNumber = eventNumber;
        return this;
    }

    public ProcessedEventTrackItemBuilder withSource(final String source) {
        this.source = source;
        return this;
    }

    public ProcessedEventTrackItemBuilder withComponentName(final String componentName) {
        this.componentName = componentName;
        return this;
    }

    public ProcessedEventTrackItem build() {
        return new ProcessedEventTrackItem(previousEventNumber, eventNumber, source, componentName);
    }
}
