package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.Optional.of;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class LinkedEvent extends Event {

    private final Long previousEventNumber;

    public LinkedEvent(
            final UUID id,
            final UUID streamId,
            final Long sequenceId,
            final String name,
            final String metadata,
            final String payload,
            final ZonedDateTime createdAt,
            final Long eventNumber,
            final Long previousEventNumber) {
        super(id, streamId, sequenceId, name, metadata, payload, createdAt, of(eventNumber));
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
        final LinkedEvent linkedEvent = (LinkedEvent) o;
        return Objects.equals(getId(), linkedEvent.getId()) &&
                Objects.equals(getStreamId(), linkedEvent.getStreamId()) &&
                Objects.equals(getSequenceId(), linkedEvent.getSequenceId()) &&
                Objects.equals(getName(), linkedEvent.getName()) &&
                Objects.equals(getPayload(), linkedEvent.getPayload()) &&
                Objects.equals(getMetadata(), linkedEvent.getMetadata()) &&
                Objects.equals(getCreatedAt(), linkedEvent.getCreatedAt()) &&
                Objects.equals(getEventNumber(), linkedEvent.getEventNumber()) &&
                Objects.equals(previousEventNumber, linkedEvent.previousEventNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStreamId(), getSequenceId(), getName(), getPayload(), getMetadata(), getCreatedAt(), getEventNumber(), previousEventNumber);
    }

    @Override
    public String toString() {
        return "LinkedEvent{" +
                "id=" + getId() +
                ", streamId=" + getStreamId() +
                ", sequenceId=" + getSequenceId() +
                ", name='" + getName() + '\'' +
                ", payload='" + getPayload() + '\'' +
                ", metadata='" + getMetadata() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", eventNumber=" + getEventNumber() +
                ", previousEventNumber=" + previousEventNumber +
                '}';
    }
}
