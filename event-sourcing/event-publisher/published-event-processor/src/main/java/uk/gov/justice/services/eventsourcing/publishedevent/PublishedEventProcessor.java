package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.PublishedEventFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import javax.inject.Inject;

public class PublishedEventProcessor {

    @Inject
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Inject
    private EventConverter eventConverter;

    @Inject
    private PublishedEventFactory publishedEventFactory;

    @Inject
    private PublishedEventRepository publishedEventRepository;

    public void createPublishedEvent(final Event event) {

        final UUID eventId = event.getId();
        final long eventNumber = event
                .getEventNumber()
                .orElseThrow(() -> new MissingEventNumberException(format("Event with id '%s' has no event number", eventId)));

        final long previousEventNumber = publishedEventRepository.getPreviousEventNumber(
                eventId,
                eventNumber);

        final Metadata metadata = eventConverter.metadataOf(event);
        final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                metadata,
                previousEventNumber,
                eventNumber);

        final PublishedEvent publishedEvent = publishedEventFactory.create(
                event,
                updatedMetadata,
                eventNumber,
                previousEventNumber);

        publishedEventRepository.save(publishedEvent);
    }
}
