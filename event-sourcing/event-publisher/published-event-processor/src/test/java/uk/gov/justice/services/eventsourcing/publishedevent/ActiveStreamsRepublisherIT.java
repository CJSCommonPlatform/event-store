package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.nio.file.Paths.get;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.eventsourcing.EventFetcherRepository;
import uk.gov.justice.services.eventsourcing.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.prepublish.PublishedEventFactory;
import uk.gov.justice.services.eventsourcing.publishedevent.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.publishedevent.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.publishedevent.helpers.TestEventStreamInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class ActiveStreamsRepublisherIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    private final TestEventInserter testEventInserter = new TestEventInserter();
    private final TestEventStreamInserter testEventStreamInserter = new TestEventStreamInserter();

    private final EventFactory eventFactory = new EventFactory();

    private final EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory = new EventStreamJdbcRepositoryFactory();
    private final EventJdbcRepositoryFactory eventJdbcRepositoryFactory = new EventJdbcRepositoryFactory();

    private final PublishedEventProcessor publishedEventProcessor = new PublishedEventProcessor();
    private final PublishedEventsProcessor publishedEventsProcessor = new PublishedEventsProcessor();

    private final ActiveStreamsRepublisher activeStreamsRepublisher = new ActiveStreamsRepublisher();

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);

        setField(activeStreamsRepublisher, "publishedEventsProcessor", publishedEventsProcessor);
        setField(activeStreamsRepublisher, "eventStreamJdbcRepositoryFactory", eventStreamJdbcRepositoryFactory);
        setField(activeStreamsRepublisher, "eventJdbcRepositoryFactory", eventJdbcRepositoryFactory);
        setField(activeStreamsRepublisher, "defaultEventSourceDefinitionFactory", new DefaultEventSourceDefinitionFactory());
        setField(activeStreamsRepublisher, "jdbcDataSourceProvider", (JdbcDataSourceProvider) jndiName -> eventStoreDataSource);

        setUpPublishedEventProcessor(publishedEventProcessor);
        setUpPublishedEventsProcessor(publishedEventsProcessor, publishedEventProcessor);
        setUpEventStreamJdbcRepositoryFactory();
    }

    @Test
    public void shouldPopulatePublishedEvents() throws Exception {

        final String name = "example.first-event";
        final long sequenceId = 1L;
        final long eventNumber = 1L;
        final UUID streamId = randomUUID();
        final UUID eventId = randomUUID();
        final Event event = eventFactory.createEvent(streamId, eventId, name, sequenceId, eventNumber);
        testEventInserter.insertIntoEventLog(event);

        final long positionInStream = 1l;
        final boolean active = true;
        testEventStreamInserter.insertIntoEventStream(streamId, positionInStream, active, ZonedDateTime.now());

        try (final Connection connection = eventStoreDataSource.getConnection()) {

            activeStreamsRepublisher.populatePublishedEvents();

            final Optional<PublishedEvent> publishedEventOptional = new EventFetcherRepository().getPublishedEvent(eventId, connection);

            if (publishedEventOptional.isPresent()) {
                final PublishedEvent actual = publishedEventOptional.get();
                assertThat(actual.getId(), is(event.getId()));
                assertThat(actual.getName(), is(name));
                assertThat(actual.getPreviousEventNumber(), is(0L));
                assertThat(actual.getEventNumber().get(), is(1L));
                assertThat(actual.getSequenceId(), is(sequenceId));
            } else {
                fail();
            }
        }
    }

    private SubscriptionDataSourceProvider setUpSubscriptionDataSourceProvider() throws MalformedURLException {

        final SubscriptionDataSourceProvider subscriptionDataSourceProvider = new SubscriptionDataSourceProvider();
        final TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();

        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();
        final URL url = getFromClasspath("yaml/event-sources.yaml");
        final Location location = new Location(null, null, of(url.toString()));

        testJdbcDataSourceProvider.setDataSource(eventStoreDataSource);
        eventSourceDefinitionRegistry.register(new EventSourceDefinition("", true, location));

        setField(subscriptionDataSourceProvider, "jdbcDataSourceProvider", testJdbcDataSourceProvider);
        setField(subscriptionDataSourceProvider, "eventSourceDefinitionRegistry", eventSourceDefinitionRegistry);

        setField(eventJdbcRepositoryFactory, "eventInsertionStrategy", new AnsiSQLEventLogInsertionStrategy());
        setField(eventJdbcRepositoryFactory, "jdbcResultSetStreamer", new JdbcResultSetStreamer());
        setField(eventJdbcRepositoryFactory, "preparedStatementWrapperFactory", new PreparedStatementWrapperFactory());

        return subscriptionDataSourceProvider;
    }


    private void setUpPublishedEventsProcessor(final PublishedEventsProcessor publishedEventsProcessor,
                                               final PublishedEventProcessor publishedEventProcessor) throws MalformedURLException {
        setField(publishedEventsProcessor, "publishedEventInserter", new PublishedEventInserter());
        setField(publishedEventsProcessor, "publishedEventProcessor", publishedEventProcessor);
        setField(publishedEventsProcessor, "subscriptionDataSourceProvider", setUpSubscriptionDataSourceProvider());
    }

    private void setUpPublishedEventProcessor(final PublishedEventProcessor publishedEventProcessor) throws MalformedURLException {

        final EventConverter eventConverter = new EventConverter();

        setField(publishedEventProcessor, "metadataEventNumberUpdater", new MetadataEventNumberUpdater());
        setField(publishedEventProcessor, "eventConverter", eventConverter);
        setField(publishedEventProcessor, "prePublishRepository", new PrePublishRepository());
        setField(publishedEventProcessor, "publishedEventFactory", new PublishedEventFactory());
        setField(publishedEventProcessor, "publishedEventInserter", new PublishedEventInserter());
        setField(eventConverter, "stringToJsonObjectConverter", new StringToJsonObjectConverter());
        setField(publishedEventProcessor, "eventConverter", eventConverter);
        setField(publishedEventProcessor, "subscriptionDataSourceProvider", setUpSubscriptionDataSourceProvider());
    }

    private void setUpEventStreamJdbcRepositoryFactory() {
        setField(eventStreamJdbcRepositoryFactory, "jdbcResultSetStreamer", new JdbcResultSetStreamer());
        setField(eventStreamJdbcRepositoryFactory, "preparedStatementWrapperFactory", new PreparedStatementWrapperFactory());
    }

    private URL getFromClasspath(final String name) throws MalformedURLException {
        return get(getClass().getClassLoader().getResource(name).getPath()).toUri().toURL();
    }
}
