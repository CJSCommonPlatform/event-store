package uk.gov.justice.services.eventsourcing.linkedevent;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.prepublish.LinkedEventFactory;
import uk.gov.justice.services.eventsourcing.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEventJdbcRepository;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

public class LinkedEventProcessor {

    @Inject
    SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Inject
    MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Inject
    EventConverter eventConverter;

    @Inject
    PrePublishRepository prePublishRepository;

    @Inject
    LinkedEventFactory linkedEventFactory;

    @Inject
    LinkedEventJdbcRepository linkedEventJdbcRepository;


    public void createLinkedEvent(final Event event) throws LinkedEventSQLException {

        final Long eventNumber = event.getEventNumber().get();

        final long previousEventNumber;
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            previousEventNumber = prePublishRepository.getPreviousEventNumber(eventNumber, connection);
        } catch (final SQLException e) {
            throw new LinkedEventSQLException(format("Unable to get previous event number %d ", eventNumber), e);
        }

        final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                eventConverter.metadataOf(event),
                previousEventNumber,
                eventNumber);

        final LinkedEvent linkedEvent = linkedEventFactory.create(event, updatedMetadata, eventNumber, previousEventNumber);
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            linkedEventJdbcRepository.insertLinkedEvent(linkedEvent, connection);

        } catch (final SQLException e) {
            throw new LinkedEventSQLException(format("Unable to insert linked event previous event number %d ", linkedEvent.getPreviousEventNumber()), e);
        }
    }

}
