package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration;

import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.postgres.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataFrom;
import static uk.gov.justice.services.test.utils.events.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.DatabaseTableTruncator;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventTableCleaner;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.ActiveEventStreamIdProvider;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.EventNumberRenumberer;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventConverter;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers.EventInserter;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers.StreamIdGenerator;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers.StreamStatusInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategyProducer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.events.TestEventInserter;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.OpenEjbEventStoreDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.SequenceSetter;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.sql.DataSource;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Default;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class RebuildPublishedEventIT {


    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter(eventStoreDataSource);
    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();
    private final SequenceSetter sequenceSetter = new SequenceSetter();
    private final StreamIdGenerator streamIdGenerator = new StreamIdGenerator();
    private final StreamStatusInserter streamStatusInserter = new StreamStatusInserter(eventStoreDataSource);
    private final EventInserter eventInserter = new EventInserter(eventStoreDataSource);

    private static final int CURRENT_EVENT_START_NUMBER = 1000;

    private static int port;

    @BeforeClass
    public static void beforeClass() {
        port = getNextAvailablePort();
    }

    @Before
    public void cleanDatabase() {
        databaseCleaner.cleanEventStoreTables("framework");
    }

    @Configuration
    public Properties postgresqlConfiguration() {
        return createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addHttpEjbPort(port)
                .addPostgresqlEventStore()
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            PublishedEventRebuilder.class,
            EventNumberRenumberer.class,
            EventJdbcRepository.class,
            PublishedEventTableCleaner.class,
            PublishedEventConverter.class,
            PublishedEventRepository.class,
            ActiveEventStreamIdProvider.class,
            OpenEjbEventStoreDataSourceProvider.class,
            EventInsertionStrategyProducer.class,
            DatabaseTableTruncator.class,
            EventStreamJdbcRepository.class,
            JdbcResultSetStreamer.class,
            PreparedStatementWrapperFactory.class,
            UtcClock.class,
            EventConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            StringToJsonObjectConverter.class,
            ObjectMapperProducer.class,
            LoggerProducer.class
    })
    @Default
    public WebApp war() {
        return new WebApp();
    }

    @Inject
    private PublishedEventRebuilder publishedEventRebuilder;

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    public void shouldRenumberEventsInEventLogTruncatePublishedEventsAndUpdatePublishedEventFromEventLog() throws Exception {

        sequenceSetter.setSequenceTo(CURRENT_EVENT_START_NUMBER, "event_sequence_seq", eventStoreDataSource);

        testEventInserter.insertIntoPublishedEvent(publishedEventBuilder().withEventNumber(1001).withPreviousEventNumber(1000).build());
        testEventInserter.insertIntoPublishedEvent(publishedEventBuilder().withEventNumber(1002).withPreviousEventNumber(1001).build());
        testEventInserter.insertIntoPublishedEvent(publishedEventBuilder().withEventNumber(1003).withPreviousEventNumber(1002).build());
        testEventInserter.insertIntoPublishedEvent(publishedEventBuilder().withEventNumber(1004).withPreviousEventNumber(1003).build());
        testEventInserter.insertIntoPublishedEvent(publishedEventBuilder().withEventNumber(1005).withPreviousEventNumber(1004).build());

        final int numberOfStreams = 10;
        final int numberOfEvents = 20;

        final List<UUID> streamIds = streamIdGenerator.generateStreamIds(numberOfStreams);
        streamIds.forEach(streamStatusInserter::insertStreamStatus);

        eventInserter.insertSomeEvents(numberOfEvents, streamIds);

        final List<Event> events = testEventInserter.findAllEvents();

        publishedEventRebuilder.rebuild();

        final List<PublishedEvent> publishedEvents = testEventInserter.findAllPublishedEvents();

        assertThat(publishedEvents.size(), is(numberOfEvents));

        for (int i = 0; i < numberOfEvents; i++) {
            final long previousEventNumber = i;
            final long currentEventNumber = i + 1;

            final Metadata expectedMetadata = createExpectedMetadataFrom(events.get(i).getMetadata(), previousEventNumber, currentEventNumber);

            assertThat(publishedEvents.get(i).getName(), is(events.get(i).getName()));
            assertThat(publishedEvents.get(i).getEventNumber().orElse(-1L), is(currentEventNumber));
            assertThat(publishedEvents.get(i).getPreviousEventNumber(), is(previousEventNumber));

            assertThat(publishedEvents.get(i).getStreamId(), is(events.get(i).getStreamId()));
            assertThat(publishedEvents.get(i).getPayload(), is(events.get(i).getPayload()));
            assertThat(publishedEvents.get(i).getMetadata(), is(expectedMetadata.asJsonObject().toString()));
            assertThat(publishedEvents.get(i).getCreatedAt(), is(events.get(i).getCreatedAt()));
            assertThat(publishedEvents.get(i).getPositionInStream(), is(events.get(i).getPositionInStream()));
        }

        assertThat(sequenceSetter.getCurrentSequenceValue("event_sequence_seq", eventStoreDataSource), is((long) numberOfEvents));
    }

    private Metadata createExpectedMetadataFrom(final String metadata, final long previousEventNumber, final long currentEventNumber) {

        final JsonObject metadataJsonObject = Json.createReader(new StringReader(metadata)).readObject();
        return metadataFrom(metadataJsonObject)
                .withEventNumber(currentEventNumber)
                .withPreviousEventNumber(previousEventNumber)
                .build();
    }
}
