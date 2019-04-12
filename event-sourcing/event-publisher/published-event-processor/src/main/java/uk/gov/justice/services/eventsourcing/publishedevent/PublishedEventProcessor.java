package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.prepublish.PublishedEventFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventJdbcRepository;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

public class PublishedEventProcessor {

    @Inject
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Inject
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Inject
    private EventConverter eventConverter;

    @Inject
    private PrePublishRepository prePublishRepository;

    @Inject
    private PublishedEventFactory publishedEventFactory;

    @Inject
    private PublishedEventJdbcRepository publishedEventJdbcRepository;

    public void createPublishedEvent(final Event event) throws PublishedEventSQLException {

        final long eventNumber = event.getEventNumber().get();

        final long previousEventNumber;
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            previousEventNumber = prePublishRepository.getPreviousEventNumber(eventNumber, connection);
        } catch (final SQLException e) {
            throw new PublishedEventSQLException(format("Unable to get previous event number for event with id '%s'", event.getId()), e);
        }

        final Metadata metadata = eventConverter.metadataOf(event);
        final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                metadata,
                previousEventNumber,
                eventNumber);

        final PublishedEvent publishedEvent = publishedEventFactory.create(event, updatedMetadata, eventNumber, previousEventNumber);
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            publishedEventJdbcRepository.insertPublishedEvent(publishedEvent, connection);

        } catch (final SQLException e) {
            throw new PublishedEventSQLException(format("Unable to insert PublishedEvent with id '%s'", event.getId()), e);
        }
    }

}
