package uk.gov.justice.services.eventsourcing.publishedevent;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.sql.DataSource;

public class ActiveStreamsRepublisher {

    @Inject
    private PublishedEventsProcessor publishedEventsProcessor;

    @Inject
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Inject
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Inject
    private DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    @Inject
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    public void populatePublishedEvents() {

        final String defaultDataSourceJndiName = defaultEventSourceDefinitionFactory.createDefaultEventSource()
                .getLocation()
                .getDataSource()
                .orElseThrow(() -> new MissingDataSourceNameException("Unable to create DataSource. Default DataSource name not found in event source definition."));

        final DataSource defaultDataSource = jdbcDataSourceProvider.getDataSource(defaultDataSourceJndiName);

        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(defaultDataSource);
        try (final Stream<EventStream> activeStreams = eventStreamJdbcRepository.findActive()) {
            activeStreams.forEach(stream -> {
                publishedEventsProcessor.populatePublishedEvents(stream.getStreamId(), eventJdbcRepositoryFactory.eventJdbcRepository(defaultDataSource));
            });
        }
    }
}
