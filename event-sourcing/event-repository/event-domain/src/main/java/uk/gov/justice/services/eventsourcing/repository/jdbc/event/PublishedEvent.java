package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Optional.of;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class PublishedEvent extends Event {

    private final Long previousEventNumber;

    public PublishedEvent(
            final UUID id,
            final UUID streamId,
            final Long positionInStream,
            final String name,
            final String metadata,
            final String payload,
            final ZonedDateTime createdAt,
            final Long eventNumber,
            final Long previousEventNumber) {
        super(id, streamId, positionInStream, name, metadata, payload, createdAt, of(eventNumber));
        this.previousEventNumber = previousEventNumber;
    }

    public Long getPreviousEventNumber() {
        return previousEventNumber;
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PublishedEvent publishedEvent = (PublishedEvent) o;
        return Objects.equals(getId(), publishedEvent.getId()) &&
                Objects.equals(getStreamId(), publishedEvent.getStreamId()) &&
                Objects.equals(getPositionInStream(), publishedEvent.getPositionInStream()) &&
                Objects.equals(getName(), publishedEvent.getName()) &&
                Objects.equals(getPayload(), publishedEvent.getPayload()) &&
                Objects.equals(getMetadata(), publishedEvent.getMetadata()) &&
                Objects.equals(getCreatedAt(), publishedEvent.getCreatedAt()) &&
                Objects.equals(getEventNumber(), publishedEvent.getEventNumber()) &&
                Objects.equals(previousEventNumber, publishedEvent.previousEventNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStreamId(), getPositionInStream(), getName(), getPayload(), getMetadata(), getCreatedAt(), getEventNumber(), previousEventNumber);
    }

    @Override
    public String toString() {
        return "PublishedEvent{" +
                "id=" + getId() +
                ", streamId=" + getStreamId() +
                ", positionInStream=" + getPositionInStream() +
                ", name='" + getName() + '\'' +
                ", payload='" + getPayload() + '\'' +
                ", metadata='" + getMetadata() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", eventNumber=" + getEventNumber() +
                ", previousEventNumber=" + previousEventNumber +
                '}';
    }
}
