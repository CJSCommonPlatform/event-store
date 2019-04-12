package uk.gov.justice.services.eventsourcing.publishedevent;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public class ActiveStreamsProcessor {

    @Inject
    private PublishedEventsProcessor publishedEventsProcessor;

    @Inject
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    private DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    public void populatePublishedEvents() {

        final Optional<String> dataSource;
        dataSource = defaultEventSourceDefinitionFactory.createDefaultEventSource().getLocation().getDataSource();

        final String jndiDatasource = dataSource.get();
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);
        try (final Stream<EventStream> activeStreams = eventStreamJdbcRepository.findActive()) {
            activeStreams.forEach(stream -> {
                publishedEventsProcessor.populatePublishedEvents(stream.getStreamId(), eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource));
            });
        }
    }
}
