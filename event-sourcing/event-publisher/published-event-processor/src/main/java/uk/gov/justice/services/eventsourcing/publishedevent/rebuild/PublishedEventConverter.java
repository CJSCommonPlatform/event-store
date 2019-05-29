package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.UUID;

public class PublishedEventConverter {

    public PublishedEvent toPublishedEvent(final Event event, final long previousEventNumber) {

        final UUID eventId = event.getId();
        final Long eventNumber = event.getEventNumber()
                .orElseThrow(() -> new RebuildException(format("No event number found for event with id '%s'", eventId)));

        return new PublishedEvent(
                eventId,
                event.getStreamId(),
                event.getPositionInStream(),
                event.getName(),
                event.getMetadata(),
                event.getPayload(),
                event.getCreatedAt(),
                eventNumber,
                previousEventNumber
        );
    }

}
