package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import javax.inject.Inject;

public class PublishedEventConverter {

    @Inject
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Inject
    private EventConverter eventConverter;

    public PublishedEvent toPublishedEvent(final Event event, final long previousEventNumber) {

        final UUID eventId = event.getId();
        final Long eventNumber = event.getEventNumber()
                .orElseThrow(() -> new RebuildException(format("No event number found for event with id '%s'", eventId)));

        final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                eventConverter.metadataOf(event),
                previousEventNumber,
                eventNumber);

        return new PublishedEvent(
                eventId,
                event.getStreamId(),
                event.getPositionInStream(),
                event.getName(),
                updatedMetadata.asJsonObject().toString(),
                event.getPayload(),
                event.getCreatedAt(),
                eventNumber,
                previousEventNumber
        );
    }

}
