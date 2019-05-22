package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.events.TestEventInserter;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventDeQueuerIT {

    private final DataSource dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter(dataSource);
    private final Clock clock = new UtcClock();

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @InjectMocks
    private EventDeQueuer eventDeQueuer;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(dataSource);
    }

    @Test
    public void shouldPopEventsFromThePrePublishQueue() throws Exception {

        final String tableName = "pre_publish_queue";

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));

        final Event event_1 = eventBuilder().withName("example.first-event").withSequenceId(1L).build();
        final Event event_2 = eventBuilder().withName("example.second-event").withSequenceId(2L).build();
        final Event event_3 = eventBuilder().withName("example.third-event").withSequenceId(3L).build();

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);

        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_1.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_2.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_3.getId()));

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));
    }

    @Test
    public void shouldPopEventsFromThePublishQueue() throws Exception {

        final String tableName = "publish_queue";

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));

        final Event event_1 = eventBuilder().withName("example.first-event").withSequenceId(1L).build();
        final Event event_2 = eventBuilder().withName("example.second-event").withSequenceId(2L).build();
        final Event event_3 = eventBuilder().withName("example.third-event").withSequenceId(3L).build();

        insertInPublishQueue(event_1,  event_2, event_3);

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);

        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_1.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_2.getId()));
        assertThat(eventDeQueuer.popNextEventId(tableName).get(), is(event_3.getId()));

        assertThat(eventDeQueuer.popNextEventId(tableName).isPresent(), is(false));
    }

    private void insertInPublishQueue(final Event... events) throws SQLException {
        try(final Connection connection = dataSource.getConnection()) {

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
