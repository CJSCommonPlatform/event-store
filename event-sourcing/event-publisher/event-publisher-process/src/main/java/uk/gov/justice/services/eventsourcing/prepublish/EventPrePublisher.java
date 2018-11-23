package uk.gov.justice.services.eventsourcing.prepublish;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.MANDATORY;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.PublishQueueException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class EventPrePublisher {

    @Inject
    SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Inject
    MetadataSequenceNumberUpdater metadataSequenceNumberUpdater;

    @Inject
    PrePublishRepository prePublishRepository;

    @Inject
    UtcClock clock;

    @Inject
    EventConverter eventConverter;

    @Transactional(MANDATORY)
    public void prePublish(final Event event) {

        final UUID eventId = event.getId();

        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            final long sequenceNumber = prePublishRepository.getSequenceNumber(eventId, connection);
            final long previousSequenceNumber = prePublishRepository.getPreviousSequenceNumber(sequenceNumber, connection);

            final Metadata updatedMetadata = metadataSequenceNumberUpdater.updateMetadataJson(
                    eventConverter.metadataOf(event),
                    previousSequenceNumber,
                    sequenceNumber);

            prePublishRepository.updateMetadata(eventId, updatedMetadata.asJsonObject().toString(), connection);
            prePublishRepository.addToPublishQueueTable(eventId, clock.now(), connection);

        } catch (final SQLException e) {
            throw new PublishQueueException(format("Failed to insert event_number into metadata in event_log table for event id %s", eventId), e);
        }
    }
}
