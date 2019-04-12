package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.randomUUID;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;
import java.util.UUID;

public class PublishedEventBuilder {

    private UUID id = randomUUID();
    private UUID streamId = randomUUID();
    private Long sequenceId = 5L;
    private String name = "Test Name";
    private String metadataJSON = "{\"field\": \"Value\"}";
    private String payloadJSON = "{\"field\": \"Value\"}";
    private ZonedDateTime timestamp = new UtcClock().now();
    private Long eventNumber = 23L;
    private Long previousEventNumber = 22L;

    protected PublishedEventBuilder() {}

    public static PublishedEventBuilder publishedEventBuilder() {
        return new PublishedEventBuilder();
    }

    public PublishedEventBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public PublishedEventBuilder withStreamId(final UUID streamId) {
        this.streamId = streamId;
        return this;
    }

    public PublishedEventBuilder withSequenceId(final Long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    public PublishedEventBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public PublishedEventBuilder withMetadataJSON(final String metadataJSON) {
        this.metadataJSON = metadataJSON;
        return this;
    }

    public PublishedEventBuilder withPayloadJSON(final String payloadJSON) {
        this.payloadJSON = payloadJSON;
        return this;
    }

    public PublishedEventBuilder withTimestamp(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public PublishedEventBuilder withEventNumber(final long eventNumber) {
        this.eventNumber = eventNumber;
        return this;
    }

    public PublishedEventBuilder withPreviousEventNumber(final long previousEventNumber) {
        this.previousEventNumber = previousEventNumber;
        return this;
    }

    public PublishedEvent build() {
        return new PublishedEvent(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp, eventNumber, previousEventNumber);
    }
}
