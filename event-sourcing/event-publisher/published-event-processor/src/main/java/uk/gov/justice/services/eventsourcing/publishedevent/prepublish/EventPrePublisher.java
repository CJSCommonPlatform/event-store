package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.MANDATORY;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventQueries;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.messaging.Metadata;

import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

public class EventPrePublisher {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Inject
    private PrePublishRepository prePublishRepository;

    @Inject
    private PublishedEventQueries publishedEventQueries;

    @Inject
    private UtcClock clock;

    @Inject
    private EventConverter eventConverter;

    @Inject
    private PublishedEventFactory publishedEventFactory;

    @Transactional(MANDATORY)
    public void prePublish(final Event event) {

        final UUID eventId = event.getId();

        final DataSource defaultDataSource = eventStoreDataSourceProvider.getDefaultDataSource();
        try {
            final long eventNumber = prePublishRepository.getEventNumber(eventId, defaultDataSource);
            final long previousEventNumber = prePublishRepository.getPreviousEventNumber(eventNumber, defaultDataSource);

            final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                    eventConverter.metadataOf(event),
                    previousEventNumber,
                    eventNumber);

            final PublishedEvent publishedEvent = publishedEventFactory.create(
                    event,
                    updatedMetadata,
                    eventNumber,
                    previousEventNumber);

            publishedEventQueries.insertPublishedEvent(publishedEvent, defaultDataSource);
            prePublishRepository.addToPublishQueueTable(eventId, clock.now(), defaultDataSource);

        } catch (final SQLException e) {
            throw new PublishedEventException(format("Failed to insert event_number into metadata in event_log table for event id %s", eventId), e);
        }
    }
}
