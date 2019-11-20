package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration;

import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.postgres.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;

import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.DatabaseTableTruncator;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventTableCleaner;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.ActiveEventStreamIdProvider;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventConverter;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers.EventInserter;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers.StreamIdGenerator;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers.StreamStatusInserter;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber.EventNumberRenumberer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategyProducer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.util.io.Closer;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.spi.DefaultEnvelopeProvider;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;
import uk.gov.justice.services.test.utils.events.EventStoreDataAccess;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.OpenEjbEventStoreDataSourceProvider;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
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
public class RebuildShouldIgnoreInactiveStreamsIT {


    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final EventStoreDataAccess eventStoreDataAccess = new EventStoreDataAccess(eventStoreDataSource);
    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();
    private final StreamIdGenerator streamIdGenerator = new StreamIdGenerator();
    private final StreamStatusInserter streamStatusInserter = new StreamStatusInserter(eventStoreDataSource);
    private final EventInserter eventInserter = new EventInserter(eventStoreDataSource);

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
            LoggerProducer.class,
            DefaultJsonEnvelopeProvider.class,
            DefaultEnvelopeProvider.class,
            Closer.class
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

        final int numberOfEvents = 10;

        final List<UUID> streamIds = streamIdGenerator.generateStreamIds(2);

        final UUID activeStreamId = streamIds.get(0);
        final UUID inactiveStreamId = streamIds.get(1);

        streamIds.forEach(streamStatusInserter::insertStreamStatus);
        eventInserter.insertSomeEvents(numberOfEvents, streamIds);

        streamStatusInserter.setStreamInactive(inactiveStreamId);

        // every other event should now belong to an inactive stream

        publishedEventRebuilder.rebuild();

        final List<PublishedEvent> publishedEvents = eventStoreDataAccess.findAllPublishedEventsOrderedByEventNumber();

        assertThat(publishedEvents.size(), is(numberOfEvents / 2));

        final AtomicLong previousEventNumber = new AtomicLong(0);

        publishedEvents.forEach(publishedEvent -> {
            assertThat(publishedEvent.getPreviousEventNumber(), is(previousEventNumber.get()));
            assertThat(publishedEvent.getStreamId(), is(activeStreamId));
            previousEventNumber.set(publishedEvent.getEventNumber().orElse(-1L));
        });
    }
}
