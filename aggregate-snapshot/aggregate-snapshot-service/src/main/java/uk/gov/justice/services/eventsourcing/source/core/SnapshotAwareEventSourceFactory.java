package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import javax.inject.Inject;

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
    private EventConverter eventConverter;

    @Inject
    private SnapshotService snapshotService;

    @Inject
    private LinkedEventFinder linkedEventFinder;

    public EventSource create(final String jndiDatasource, final String eventSourceName) {

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                linkedEventFinder);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository, eventSourceName);

        return new SnapshotAwareEventSource(
                eventStreamManager,
                eventRepository,
                snapshotService,
                eventConverter,
                eventSourceName);
    }
}
