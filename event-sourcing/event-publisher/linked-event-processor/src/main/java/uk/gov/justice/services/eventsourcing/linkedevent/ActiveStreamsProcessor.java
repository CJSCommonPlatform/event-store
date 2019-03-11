package uk.gov.justice.services.eventsourcing.linkedevent;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.subscription.registry.DefaultEventSourceDefinitionFactory;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public class ActiveStreamsProcessor {

    @Inject
    LinkedEventsProcessor linkedEventsProcessor;

    @Inject
    EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    public void populateLinkedEvents() {

        final Optional<String> dataSource;
        dataSource = defaultEventSourceDefinitionFactory.createDefaultEventSource().getLocation().getDataSource();

        final String jndiDatasource = dataSource.get();
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);
        try (final Stream<EventStream> activeStreams = eventStreamJdbcRepository.findActive()) {
            activeStreams.forEach(stream -> {
                linkedEventsProcessor.populateLinkedEvents(stream.getStreamId(), eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource));
            });
        }
    }
}