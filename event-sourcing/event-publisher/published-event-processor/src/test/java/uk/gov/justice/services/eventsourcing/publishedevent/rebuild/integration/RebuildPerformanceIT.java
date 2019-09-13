package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration;

import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
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
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.OpenEjbEventStoreDataSourceProvider;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
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
public class RebuildPerformanceIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
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
            LoggerProducer.class
    })
    @Default
    public WebApp war() {
        return new WebApp();
    }

    @Inject
    private PublishedEventRebuilder publishedEventRebuilder;

    @Test
    public void shouldRebuildPublishedEvents() throws Exception {

        final int numberOfStreams = 10;
        final int numberOfEvents = 20;

        final List<UUID> streamIds = streamIdGenerator.generateStreamIds(numberOfStreams);
        streamIds.forEach(streamStatusInserter::insertStreamStatus);

        final StopWatch stopWatch = new StopWatch();

        System.out.println("Inserting " + numberOfEvents + " events into event_log");

        stopWatch.start();
        eventInserter.insertSomeEvents(numberOfEvents, streamIds);
        stopWatch.stop();

        System.out.println(numberOfEvents + " events inserted into event_log");
        System.out.println("Inserting " + numberOfEvents + " events took " + stopWatch.getTime() + " milliseconds");

        stopWatch.reset();
        stopWatch.start();
        publishedEventRebuilder.rebuild();
        stopWatch.stop();

        final long time = stopWatch.getTime();

        final float eventsPerSecond = ((float) numberOfEvents / time) * 1000;

        System.out.println("Rebuild took " + time + " milliseconds to rebuild " + numberOfEvents + " events");
        System.out.println("Processed " + eventsPerSecond + " events per second");
    }
}
