package uk.gov.justice.services.eventsourcing.linkedevent;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEventJdbcRepository;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

public class LinkedEventsProcessor {
    @Inject
    SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Inject
    LinkedEventProcessor linkedEventProcessor;

    @Inject
    private LinkedEventJdbcRepository linkedEventJdbcRepository;


    public void populateLinkedEvents(final UUID streamId, final EventJdbcRepository eventJdbcRepository) throws LinkedEventSQLException {

            final Stream<Event> events = eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId);
            events.forEach(event -> {
                linkedEventProcessor.createLinkedEvent(event);
            });
    }

    public void truncateLinkedEvents() throws LinkedEventSQLException {
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            linkedEventJdbcRepository.truncate(connection);
        } catch (final SQLException e) {
            throw new LinkedEventSQLException("Failed to truncate Linked Events table", e);
        }
    }
}
