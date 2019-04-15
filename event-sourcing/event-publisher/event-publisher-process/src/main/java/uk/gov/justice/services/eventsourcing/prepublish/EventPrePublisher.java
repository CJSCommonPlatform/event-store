package uk.gov.justice.services.eventsourcing.prepublish;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.MANDATORY;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.PublishQueueException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventInserter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class EventPrePublisher {

    @Inject
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Inject
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Inject
    private PrePublishRepository prePublishRepository;

    @Inject
    private PublishedEventInserter publishedEventInserter;

    @Inject
    private UtcClock clock;

    @Inject
    private EventConverter eventConverter;

    @Inject
    private PublishedEventFactory publishedEventFactory;

    @Transactional(MANDATORY)
    public void prePublish(final Event event) {

        final UUID eventId = event.getId();

        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            final long eventNumber = prePublishRepository.getEventNumber(eventId, connection);
            final long previousEventNumber = prePublishRepository.getPreviousEventNumber(eventNumber, connection);

            final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                    eventConverter.metadataOf(event),
                    previousEventNumber,
                    eventNumber);

            final PublishedEvent publishedEvent = publishedEventFactory.create(
                    event,
                    updatedMetadata,
                    eventNumber,
                    previousEventNumber);

            publishedEventInserter.insertPublishedEvent(publishedEvent, connection);
            prePublishRepository.addToPublishQueueTable(eventId, clock.now(), connection);

        } catch (final SQLException e) {
            throw new PublishQueueException(format("Failed to insert event_number into metadata in event_log table for event id %s", eventId), e);
        }
    }
}
