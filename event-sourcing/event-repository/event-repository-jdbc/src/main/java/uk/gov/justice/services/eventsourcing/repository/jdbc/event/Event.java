package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.Optional.empty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity class to represent a persisted event.
 */
public class Event {

    private final UUID id;
    private final UUID streamId;
    private final Long sequenceId;
    private final String name;
    private final String payload;
    private final String metadata;
    private final ZonedDateTime createdAt;

    /**
     * Note: event number is inserted by the database using a sequence. This value will never
     * be inserted by the repositories, and before the event is inserted should always be empty
     */
    private final Optional<Long> eventNumber;

    public Event(final UUID id,
                 final UUID streamId,
                 final Long sequenceId,
                 final String name,
                 final String metadata,
                 final String payload,
                 final ZonedDateTime createdAt) {
        this(
                id,
                streamId,
                sequenceId,
                name,
                metadata,
                payload,
                createdAt,
                empty()
        );
    }

    public Event(final UUID id,
          final UUID streamId,
          final Long sequenceId,
          final String name,
          final String metadata,
          final String payload,
          final ZonedDateTime createdAt,
          final Optional<Long> eventNumber) {
        this.id = id;
        this.streamId = streamId;
        this.sequenceId = sequenceId;
        this.name = name;
        this.metadata = metadata;
        this.payload = payload;
        this.createdAt = createdAt;
        this.eventNumber = eventNumber;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public String getPayload() {
        return payload;
    }

    public String getName() {
        return name;
    }

    public String getMetadata() {
        return metadata;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * The event number inserted by a sequence in the database. Before insertion an event should
     * always have an empty event number
     *
     * @return The event_number if it exists
     */
    public Optional<Long> getEventNumber() {
        return eventNumber;
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Event event = (Event) o;
        return Objects.equals(id, event.id) &&
                Objects.equals(streamId, event.streamId) &&
                Objects.equals(sequenceId, event.sequenceId) &&
                Objects.equals(name, event.name) &&
                Objects.equals(payload, event.payload) &&
                Objects.equals(metadata, event.metadata) &&
                Objects.equals(createdAt, event.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, streamId, sequenceId, name, payload, metadata, createdAt, eventNumber);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", streamId=" + streamId +
                ", sequenceId=" + sequenceId +
                ", name='" + name + '\'' +
                ", payload='" + payload + '\'' +
                ", metadata='" + metadata + '\'' +
                ", createdAt=" + createdAt +
                ", eventNumber=" + eventNumber +
                '}';
    }
}
