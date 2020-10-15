package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.eventlog.EventLogTriggerManipulatorFactory;
import uk.gov.justice.services.test.utils.events.EventStoreDataAccess;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventDeQueuerIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final EventStoreDataAccess eventStoreDataAccess = new EventStoreDataAccess(eventStoreDataSource);
    private final Clock clock = new UtcClock();
    private final EventLogTriggerManipulator eventLogTriggerManipulator = new EventLogTriggerManipulatorFactory()
            .create(eventStoreDataSource);


    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @InjectMocks
    private EventDeQueuer eventDeQueuer;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
        eventLogTriggerManipulator.addTriggerToEventLogTable();
    }

    @After
    public void dropTrigger() {
        eventLogTriggerManipulator.removeTriggerFromEventLogTable();
    }

    @Test
    public void shouldPopEventsFromThePrePublishQueue() throws Exception {

        final String tableName = "pre_publish_queue";

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));

        final Event event_1 = eventBuilder().withName("example.first-event").withPositionInStream(1L).build();
        final Event event_2 = eventBuilder().withName("example.second-event").withPositionInStream(2L).build();
        final Event event_3 = eventBuilder().withName("example.third-event").withPositionInStream(3L).build();

        eventStoreDataAccess.insertIntoEventLog(event_1);
        eventStoreDataAccess.insertIntoEventLog(event_2);
        eventStoreDataAccess.insertIntoEventLog(event_3);

        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_1.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_2.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_3.getId()));

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));
    }

    @Test
    public void shouldPopEventsFromThePublishQueue() throws Exception {

        final String tableName = "publish_queue";

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));

        final Event event_1 = eventBuilder().withName("example.first-event").withPositionInStream(1L).build();
        final Event event_2 = eventBuilder().withName("example.second-event").withPositionInStream(2L).build();
        final Event event_3 = eventBuilder().withName("example.third-event").withPositionInStream(3L).build();

        insertInPublishQueue(event_1,  event_2, event_3);

        eventStoreDataAccess.insertIntoEventLog(event_1);
        eventStoreDataAccess.insertIntoEventLog(event_2);
        eventStoreDataAccess.insertIntoEventLog(event_3);

        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_1.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_2.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_3.getId()));

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));
    }

    @Test
    public void shouldGetTheSizeOfTheQueue() throws Exception {

        final String tableName = "publish_queue";

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        final Event event_1 = eventBuilder().withName("example.first-event").withPositionInStream(1L).build();
        final Event event_2 = eventBuilder().withName("example.second-event").withPositionInStream(2L).build();
        final Event event_3 = eventBuilder().withName("example.third-event").withPositionInStream(3L).build();

        insertInPublishQueue(event_1,  event_2, event_3);

        eventStoreDataAccess.insertIntoEventLog(event_1);
        eventStoreDataAccess.insertIntoEventLog(event_2);
        eventStoreDataAccess.insertIntoEventLog(event_3);

        assertThat(eventDeQueuer.getSizeOfQueue(tableName), is(3));
    }

    private void insertInPublishQueue(final Event... events) throws SQLException {
        try(final Connection connection = eventStoreDataSource.getConnection()) {

            final String sql = "INSERT into publish_queue (event_log_id, date_queued) VALUES (?, ?)";
            try(final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for(Event event: events) {
                    preparedStatement.setObject(1, event.getId());
                    preparedStatement.setTimestamp(2, toSqlTimestamp(clock.now()));

                    preparedStatement.executeUpdate();
                }
            }
        }
    }
}
