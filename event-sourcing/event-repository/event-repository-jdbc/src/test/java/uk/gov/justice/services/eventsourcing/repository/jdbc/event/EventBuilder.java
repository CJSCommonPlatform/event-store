package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.swing.text.html.Option;

public class EventBuilder {

    private UUID id = randomUUID();
    private UUID streamId = randomUUID();
    private Long sequenceId = 5L;
    private String name = "Test Name";
    private String metadataJSON = "{\"field\": \"Value\"}";
    private String payloadJSON = "{\"field\": \"Value\"}";
    private ZonedDateTime timestamp = new UtcClock().now();
    private Optional<Long> eventNumber = empty();

    private EventBuilder() {}

    public static EventBuilder eventBuilder() {
        return new EventBuilder();
    }

    public EventBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public EventBuilder withStreamId(final UUID streamId) {
        this.streamId = streamId;
        return this;
    }

    public EventBuilder withSequenceId(final Long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    public EventBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public EventBuilder withMetadataJSON(final String metadataJSON) {
        this.metadataJSON = metadataJSON;
        return this;
    }

    public EventBuilder withPayloadJSON(final String payloadJSON) {
        this.payloadJSON = payloadJSON;
        return this;
    }

    public EventBuilder withTimestamp(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public EventBuilder withEventNumber(final long eventNumber) {
        this.eventNumber = of(eventNumber);
        return this;
    }

    public Event build() {
        return new Event(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp, eventNumber);
    }
}
