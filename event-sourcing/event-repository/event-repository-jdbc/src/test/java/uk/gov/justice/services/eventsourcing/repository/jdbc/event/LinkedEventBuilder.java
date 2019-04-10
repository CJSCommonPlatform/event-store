package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.randomUUID;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;
import java.util.UUID;

public class LinkedEventBuilder {

    private UUID id = randomUUID();
    private UUID streamId = randomUUID();
    private Long sequenceId = 5L;
    private String name = "Test Name";
    private String metadataJSON = "{\"field\": \"Value\"}";
    private String payloadJSON = "{\"field\": \"Value\"}";
    private ZonedDateTime timestamp = new UtcClock().now();
    private Long eventNumber = 23L;
    private Long previousEventNumber = 22L;

    protected LinkedEventBuilder() {}

    public static LinkedEventBuilder linkedEventBuilder() {
        return new LinkedEventBuilder();
    }

    public LinkedEventBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public LinkedEventBuilder withStreamId(final UUID streamId) {
        this.streamId = streamId;
        return this;
    }

    public LinkedEventBuilder withSequenceId(final Long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    public LinkedEventBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public LinkedEventBuilder withMetadataJSON(final String metadataJSON) {
        this.metadataJSON = metadataJSON;
        return this;
    }

    public LinkedEventBuilder withPayloadJSON(final String payloadJSON) {
        this.payloadJSON = payloadJSON;
        return this;
    }

    public LinkedEventBuilder withTimestamp(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public LinkedEventBuilder withEventNumber(final long eventNumber) {
        this.eventNumber = eventNumber;
        return this;
    }

    public LinkedEventBuilder withPreviousEventNumber(final long previousEventNumber) {
        this.previousEventNumber = previousEventNumber;
        return this;
    }

    public LinkedEvent build() {
        return new LinkedEvent(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp, eventNumber, previousEventNumber);
    }
}
