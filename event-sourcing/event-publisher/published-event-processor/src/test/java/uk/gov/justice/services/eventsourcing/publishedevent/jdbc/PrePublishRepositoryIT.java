package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;


import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.publish.helpers.TestEventStreamInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.events.EventStoreDataAccess;
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
public class PrePublishRepositoryIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final EventStoreDataAccess eventStoreDataAccess = new EventStoreDataAccess(eventStoreDataSource);
    private final TestEventStreamInserter testEventStreamInserter = new TestEventStreamInserter();
    private final Clock clock = new UtcClock();


    @InjectMocks
    private PrePublishRepository prePublishRepository;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
    }

    @Test
    public void shouldGetTheSequenceNumberOfAnEvent() throws Exception {

        final Event event_1 = eventBuilder().withName("event-1").withPositionInStream(101L).build();
        final Event event_2 = eventBuilder().withName("event-2").withPositionInStream(102L).build();
        final Event event_3 = eventBuilder().withName("event-3").withPositionInStream(103L).build();
        final Event event_4 = eventBuilder().withName("event-4").withPositionInStream(104L).build();

        eventStoreDataAccess.insertIntoEventLog(event_1);
        eventStoreDataAccess.insertIntoEventLog(event_2);
        eventStoreDataAccess.insertIntoEventLog(event_3);
        eventStoreDataAccess.insertIntoEventLog(event_4);

        assertThat(prePublishRepository.getEventNumber(event_1.getId(), eventStoreDataSource), is(1L));
        assertThat(prePublishRepository.getEventNumber(event_2.getId(), eventStoreDataSource), is(2L));
        assertThat(prePublishRepository.getEventNumber(event_3.getId(), eventStoreDataSource), is(3L));
        assertThat(prePublishRepository.getEventNumber(event_4.getId(), eventStoreDataSource), is(4L));
    }

    @Test
    public void shouldGetThePreviousSequenceNumberOfAnEvent() throws Exception {

        final String s = "stream-1";

        final UUID streamId = randomUUID();
        testEventStreamInserter.insertIntoEventStream(streamId, 1l, true, clock.now());

        final Event event_1 = eventBuilder().withStreamId(streamId).withName("event-1").withEventNumber(1l).withPositionInStream(101L).build();
        final Event event_2 = eventBuilder().withStreamId(streamId).withName("event-2").withEventNumber(2l).withPositionInStream(102L).build();
        final Event event_3 = eventBuilder().withStreamId(streamId).withName("event-3").withEventNumber(3l).withPositionInStream(103L).build();
        final Event event_4 = eventBuilder().withStreamId(streamId).withName("event-4").withEventNumber(4l).withPositionInStream(104L).build();

        eventStoreDataAccess.insertIntoEventLog(event_1);
        eventStoreDataAccess.insertIntoEventLog(event_2);
        eventStoreDataAccess.insertIntoEventLog(event_3);
        eventStoreDataAccess.insertIntoEventLog(event_4);


        assertThat(prePublishRepository.getPreviousEventNumber(1, eventStoreDataSource), is(0L));
        assertThat(prePublishRepository.getPreviousEventNumber(2, eventStoreDataSource), is(1L));
        assertThat(prePublishRepository.getPreviousEventNumber(3, eventStoreDataSource), is(2L));
        assertThat(prePublishRepository.getPreviousEventNumber(4, eventStoreDataSource), is(3L));
    }

    @Test
    public void shouldInsertEventIdIntoThePublishTable() throws Exception {

        final UUID eventId = randomUUID();
        final ZonedDateTime now = clock.now();

            prePublishRepository.addToPublishQueueTable(eventId, now, eventStoreDataSource);


            try (final Connection connection = eventStoreDataSource.getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement("SELECT event_log_id, date_queued FROM publish_queue")) {

                final ResultSet resultSet = preparedStatement.executeQuery();

                assertThat(resultSet.next(), is(true));
                final UUID eventLogId = (UUID) resultSet.getObject("event_log_id");
                final ZonedDateTime dateQueued = fromSqlTimestamp(resultSet.getTimestamp("date_queued"));

                assertThat(eventLogId, is(eventId));
                assertThat(dateQueued, is(now));

                assertThat(resultSet.next(), is(false));
            }
    }


}
