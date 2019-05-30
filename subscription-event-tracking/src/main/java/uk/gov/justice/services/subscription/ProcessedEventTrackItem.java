package uk.gov.justice.services.subscription;

import java.util.Objects;

public class ProcessedEventTrackItem {

    private final long previousEventNumber;
    private final long eventNumber;
    private final String source;
    private final String componentName;

    public ProcessedEventTrackItem(
            final long previousEventNumber,
            final long eventNumber,
            final String source,
            final String componentName) {
        this.previousEventNumber = previousEventNumber;
        this.eventNumber = eventNumber;
        this.source = source;
        this.componentName = componentName;
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
        if (!(o instanceof ProcessedEventTrackItem)) return false;
        final ProcessedEventTrackItem that = (ProcessedEventTrackItem) o;
        return previousEventNumber == that.previousEventNumber &&
                eventNumber == that.eventNumber &&
                Objects.equals(source, that.source) &&
                Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previousEventNumber, eventNumber, source, componentName);
    }

    @Override
    public String toString() {
        return "ProcessedEventTrackItem{" +
                "previousEventNumber=" + previousEventNumber +
                ", eventNumber=" + eventNumber +
                ", source='" + source + '\'' +
                ", componentName='" + componentName + '\'' +
                '}';
    }
}
