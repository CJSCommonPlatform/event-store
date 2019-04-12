package uk.gov.justice.services.eventsourcing.prepublish.helpers;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EventFactory {

    private UtcClock clock = new UtcClock();

    public Event createEvent(final UUID streamId, final UUID eventId, final String name, final long sequenceId, final long eventNumber) {
        final String source = "event source";
        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder()
                        .withId(eventId)
                        .withName(name)
                        .withStreamId(streamId)
                        .withSource(source),
                createObjectBuilder()
                        .add("field_" + sequenceId, "value_" + sequenceId));

        final String payload = envelope.payload().toString();
        final String metadata = envelope.metadata().asJsonObject().toString();

        final ZonedDateTime createdAt = clock.now();

        return new Event(
                eventId,
                streamId,
                sequenceId,
                name,
                metadata,
                payload,
                createdAt,
                of(eventNumber));
    }

    public PublishedEvent createPublishedEvent(final UUID streamId, final String name, final long sequenceId, final long eventNumber, final long previousEventNumber) {
        final UUID eventId = randomUUID();
        final String source = "event source";
        final JsonEnvelope envelope = envelopeFrom(
                metadataBuilder()
                        .withId(eventId)
                        .withName(name)
                        .withStreamId(streamId)
                        .withSource(source),
                createObjectBuilder()
                        .add("field_" + sequenceId, "value_" + sequenceId));

        final String payload = envelope.payload().toString();
        final String metadata = envelope.metadata().asJsonObject().toString();

        final ZonedDateTime createdAt = clock.now();

        return new PublishedEvent(
                eventId,
                streamId,
                sequenceId,
                name,
                metadata,
                payload,
                createdAt,
                eventNumber,
                previousEventNumber
                );
    }
}
