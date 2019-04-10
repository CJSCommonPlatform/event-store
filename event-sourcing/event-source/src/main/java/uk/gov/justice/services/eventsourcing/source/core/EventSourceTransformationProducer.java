package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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
    private LinkedEventFinder linkedEventFinder;

    @Produces
    public EventSourceTransformation eventSourceTransformation() {
        final EventSourceDefinition defaultEventSourceDefinition = eventSourceDefinitionRegistry.getDefaultEventSourceDefinition();

        final String jndiDatasource = defaultEventSourceDefinition.getLocation().getDataSource().get();

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                linkedEventFinder);

        final EventStreamManager eventStreamManager = eventStreamManagerFactory.eventStreamManager(eventRepository, defaultEventSourceDefinition.getName());

        return new DefaultEventSourceTransformation(eventStreamManager);
    }
}
