package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinderFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class JdbcPublishedEventSourceFactory {

    @Inject
    private EventRepositoryFactory eventRepositoryFactory;

    @Inject
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    private EventConverter eventConverter;

    @Inject
    private PublishedEventFinderFactory publishedEventFinderFactory;

    @Inject
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    public JdbcBasedPublishedEventSource create(final String jndiDatasource) {

        final DataSource dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(dataSource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(dataSource);
        final PublishedEventFinder publishedEventFinder = publishedEventFinderFactory.create(dataSource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                publishedEventFinder);

        return new JdbcBasedPublishedEventSource(eventRepository, eventConverter);
    }
}
