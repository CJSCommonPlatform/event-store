package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinderFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import javax.inject.Inject;
import javax.sql.DataSource;

public class SnapshotAwareEventSourceFactory {

    @Inject
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Inject
    private EventRepositoryFactory eventRepositoryFactory;

    @Inject
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    private SnapshotService snapshotService;

    @Inject
    private PublishedEventFinderFactory publishedEventFinderFactory;

    public EventSource create(final DataSource dataSource, final String eventSourceName) {

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(dataSource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(dataSource);
        final PublishedEventFinder publishedEventFinder = publishedEventFinderFactory.create(dataSource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                publishedEventFinder);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository, eventSourceName);

        return new SnapshotAwareEventSource(
                eventStreamManager,
                eventRepository,
                snapshotService,
                eventSourceName);
    }
}
