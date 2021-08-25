package uk.gov.justice.services.eventsourcing.publishedevent.publish;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.DatabaseTableTruncator;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.sql.DataSource;

public class PublishedEventsProcessor {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Inject
    private PublishedEventProcessor publishedEventProcessor;

    @Inject
    private DatabaseTableTruncator databaseTableTruncator;


    public void populatePublishedEvents(final UUID streamId, final EventJdbcRepository eventJdbcRepository) {

            final Stream<Event> events = eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId);
            events.forEach(publishedEventProcessor::createPublishedEvent);
    }

    public void truncatePublishedEvents() {

        final DataSource defaultDataSource = eventStoreDataSourceProvider.getDefaultDataSource();

        try {
            databaseTableTruncator.truncate("published_event", defaultDataSource);
        } catch (final SQLException e) {
            throw new PublishedEventException("Failed to truncate published_event table", e);
        }
        try {
            databaseTableTruncator.truncate("pre_publish_queue", defaultDataSource);
        } catch (final SQLException e) {
            throw new PublishedEventException("Failed to truncate pre_publish_queue table", e);
        }
    }
}
