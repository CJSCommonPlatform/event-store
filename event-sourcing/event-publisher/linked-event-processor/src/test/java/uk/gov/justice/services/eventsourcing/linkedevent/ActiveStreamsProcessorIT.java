package uk.gov.justice.services.eventsourcing.linkedevent;

import static java.nio.file.Paths.get;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.eventsourcing.EventFetcherRepository;
import uk.gov.justice.services.eventsourcing.linkedevent.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.linkedevent.helpers.EventStoreInitializer;
import uk.gov.justice.services.eventsourcing.linkedevent.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.linkedevent.helpers.TestEventStreamInserter;
import uk.gov.justice.services.eventsourcing.prepublish.LinkedEventFactory;
import uk.gov.justice.services.eventsourcing.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
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

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class ActiveStreamsProcessorIT {
    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter();
    private final TestEventStreamInserter testEventStreamInserter = new TestEventStreamInserter();
    private final EventFactory eventFactory = new EventFactory();
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory = new EventStreamJdbcRepositoryFactory();
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory = new EventJdbcRepositoryFactory();
    private final ActiveStreamsProcessor activeStreamsProcessor = new ActiveStreamsProcessor();
    private final LinkedEventsProcessor linkedEventsProcessor = new LinkedEventsProcessor();
    private final LinkedEventProcessor linkedEventProcessor = new LinkedEventProcessor();

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);

        setField(activeStreamsProcessor, "linkedEventsProcessor", linkedEventsProcessor);
        setField(activeStreamsProcessor, "eventJdbcRepositoryFactory", eventJdbcRepositoryFactory);
        setField(activeStreamsProcessor, "eventStreamJdbcRepositoryFactory", eventStreamJdbcRepositoryFactory);
        setField(activeStreamsProcessor, "defaultEventSourceDefinitionFactory", new DefaultEventSourceDefinitionFactory());
        setUpLinkedEventProcessor(linkedEventProcessor);
        setUpLinkedEventsProcessor(linkedEventsProcessor, linkedEventProcessor);

    }

    private SubscriptionDataSourceProvider setUpSubscriptionDataSourceProvider() throws MalformedURLException {
        final SubscriptionDataSourceProvider subscriptionDataSourceProvider = new SubscriptionDataSourceProvider();
        final TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();
        testJdbcDataSourceProvider.setDataSource(eventStoreDataSource);
        setField(subscriptionDataSourceProvider, "jdbcDataSourceProvider", testJdbcDataSourceProvider);
        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();
        final URL url = getFromClasspath("yaml/event-sources.yaml");
        final Location location = new Location(null, null, of(url.toString()));
        eventSourceDefinitionRegistry.register(new EventSourceDefinition("", true, location));
        setField(subscriptionDataSourceProvider, "eventSourceDefinitionRegistry", eventSourceDefinitionRegistry);

        setField(eventJdbcRepositoryFactory, "eventInsertionStrategy", new AnsiSQLEventLogInsertionStrategy());
        setField(eventJdbcRepositoryFactory, "jdbcRepositoryHelper", new JdbcRepositoryHelper());
        setField(eventJdbcRepositoryFactory, "jdbcDataSourceProvider", testJdbcDataSourceProvider);

        setField(eventStreamJdbcRepositoryFactory, "eventStreamJdbcRepositoryHelper", new JdbcRepositoryHelper());
        setField(eventStreamJdbcRepositoryFactory, "jdbcDataSourceProvider", testJdbcDataSourceProvider);

        return subscriptionDataSourceProvider;
    }


    private void setUpLinkedEventsProcessor(final LinkedEventsProcessor linkedEventsProcessor,
                                            final LinkedEventProcessor linkedEventProcessor) throws MalformedURLException {
        setField(linkedEventsProcessor, "linkedEventProcessor", linkedEventProcessor);
        setField(linkedEventsProcessor, "linkedEventJdbcRepository", new LinkedEventJdbcRepository());
        setField(linkedEventsProcessor, "linkedEventProcessor", linkedEventProcessor);
        setField(linkedEventsProcessor, "subscriptionDataSourceProvider", setUpSubscriptionDataSourceProvider());
    }

    private void setUpLinkedEventProcessor(final LinkedEventProcessor linkedEventProcessor) throws MalformedURLException {
        final EventConverter eventConverter = new EventConverter();
        setField(linkedEventProcessor, "metadataEventNumberUpdater", new MetadataEventNumberUpdater());
        setField(linkedEventProcessor, "eventConverter", eventConverter);
        setField(linkedEventProcessor, "prePublishRepository", new PrePublishRepository());
        setField(linkedEventProcessor, "linkedEventFactory", new LinkedEventFactory());
        setField(linkedEventProcessor, "linkedEventJdbcRepository", new LinkedEventJdbcRepository());
        setField(eventConverter, "stringToJsonObjectConverter", new StringToJsonObjectConverter());
        setField(linkedEventProcessor, "eventConverter", eventConverter);
        setField(linkedEventProcessor, "subscriptionDataSourceProvider", setUpSubscriptionDataSourceProvider());

    }


    private URL getFromClasspath(final String name) throws MalformedURLException {
        return get(getClass().getClassLoader().getResource(name).getPath()).toUri().toURL();
    }


    @Test
    public void shouldPopulateLinkedEvents() throws Exception {

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

            activeStreamsProcessor.populateLinkedEvents();

            final Optional<LinkedEvent> linkedEventOptional = new EventFetcherRepository().getLinkedEvent(eventId, connection);

            if (linkedEventOptional.isPresent()) {
                final LinkedEvent actual = linkedEventOptional.get();
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
}