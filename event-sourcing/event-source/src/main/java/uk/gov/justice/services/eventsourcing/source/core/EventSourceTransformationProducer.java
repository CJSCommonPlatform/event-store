package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinderFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class EventSourceTransformationProducer {

    @Inject
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Inject
    private EventRepositoryFactory eventRepositoryFactory;

    @Inject
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Inject
    private PublishedEventFinderFactory publishedEventFinderFactory;

    @Inject
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @Produces
    public EventSourceTransformation eventSourceTransformation() {

        final EventSourceDefinition defaultEventSourceDefinition = eventSourceDefinitionRegistry.getDefaultEventSourceDefinition();

        final String jndiDatasource = defaultEventSourceDefinition.getLocation().getDataSource().get();

        final DataSource dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(dataSource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(dataSource);
        final PublishedEventFinder publishedEventFinder = publishedEventFinderFactory.create(dataSource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                publishedEventFinder);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository, defaultEventSourceDefinition.getName());

        return new DefaultEventSourceTransformation(eventStreamManager);
    }
}
