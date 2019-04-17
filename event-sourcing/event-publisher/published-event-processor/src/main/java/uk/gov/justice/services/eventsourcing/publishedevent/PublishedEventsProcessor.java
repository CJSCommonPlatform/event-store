package uk.gov.justice.services.eventsourcing.publishedevent;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventInserter;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

public class PublishedEventsProcessor {

    @Inject
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Inject
    private PublishedEventProcessor publishedEventProcessor;

    @Inject
    private PublishedEventInserter publishedEventInserter;


    public void populatePublishedEvents(final UUID streamId, final EventJdbcRepository eventJdbcRepository) {

            final Stream<Event> events = eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId);
            events.forEach(event -> {
                publishedEventProcessor.createPublishedEvent(event);
            });
    }

    public void truncatePublishedEvents() throws PublishedEventSQLException {
        try (final Connection connection = subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()) {
            publishedEventInserter.truncate(connection);
        } catch (final SQLException e) {
            throw new PublishedEventSQLException("Failed to truncate Linked Events table", e);
        }
    }
}
