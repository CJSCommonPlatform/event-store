package uk.gov.justice.services.eventsourcing.prepublish;


import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishing.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventStreamInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrePublishRepositoryTest {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter();
    private final TestEventStreamInserter testEventStreamInserter = new TestEventStreamInserter();
    private final EventFactory eventFactory = new EventFactory();
    private final Clock clock = new UtcClock();


    @InjectMocks
    private PrePublishRepository prePublishRepository;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
    }

    @Test
    public void shouldGetTheSequenceNumberOfAnEvent() throws Exception {

        final Event event_1 = eventFactory.createEvent("event-1", 101);
        final Event event_2 = eventFactory.createEvent("event-2", 102);
        final Event event_3 = eventFactory.createEvent("event-3", 103);
        final Event event_4 = eventFactory.createEvent("event-4", 104);

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);
        testEventInserter.insertIntoEventLog(event_4);

        try (final Connection connection = eventStoreDataSource.getConnection()) {
            assertThat(prePublishRepository.getEventNumber(event_1.getId(), connection), is(1L));
            assertThat(prePublishRepository.getEventNumber(event_2.getId(), connection), is(2L));
            assertThat(prePublishRepository.getEventNumber(event_3.getId(), connection), is(3L));
            assertThat(prePublishRepository.getEventNumber(event_4.getId(), connection), is(4L));
        }
    }

    @Test
    public void shouldGetThePreviousSequenceNumberOfAnEvent() throws Exception {

        final String s = "stream-1";

        final UUID streamId = randomUUID();
        testEventStreamInserter.insertIntoEventStream(streamId, 1l, true, clock.now());

        final Event event_1 = eventFactory.createEvent(streamId, randomUUID(), "event-1", 1l, 101);
        final Event event_2 = eventFactory.createEvent(streamId, randomUUID(), "event-2", 2l, 102);
        final Event event_3 = eventFactory.createEvent(streamId, randomUUID(), "event-3", 3l, 103);
        final Event event_4 = eventFactory.createEvent(streamId, randomUUID(), "event-4", 4l, 104);

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);
        testEventInserter.insertIntoEventLog(event_4);


        try (final Connection connection = eventStoreDataSource.getConnection()) {

            assertThat(prePublishRepository.getPreviousEventNumber(1, connection), is(0L));
            assertThat(prePublishRepository.getPreviousEventNumber(2, connection), is(1L));
            assertThat(prePublishRepository.getPreviousEventNumber(3, connection), is(2L));
            assertThat(prePublishRepository.getPreviousEventNumber(4, connection), is(3L));
        }
    }

    @Test
    public void shouldInsertEventIdIntoThePublishTable() throws Exception {

        final UUID eventId = randomUUID();
        final ZonedDateTime now = clock.now();

        try (final Connection connection = eventStoreDataSource.getConnection()) {

            prePublishRepository.addToPublishQueueTable(eventId, now, connection);


            try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, event_log_id, date_queued FROM publish_queue")) {

                final ResultSet resultSet = preparedStatement.executeQuery();

                assertThat(resultSet.next(), is(true));
                final long id = resultSet.getLong("id");
                final UUID eventLogId = (UUID) resultSet.getObject("event_log_id");
                final ZonedDateTime dateQueued = fromSqlTimestamp(resultSet.getTimestamp("date_queued"));

                assertThat(id, is(1L));
                assertThat(eventLogId, is(eventId));
                assertThat(dateQueued, is(now));

                assertThat(resultSet.next(), is(false));
            }
        }
    }


}
