package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventBuilder.eventBuilder;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.TestEventStoreDataSourceFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class EventJdbcRepositoryIT {

    private static final UUID STREAM_ID = randomUUID();
    private static final Long SEQUENCE_ID = 5L;

    private static final String FRAMEWORK_CONTEXT_NAME = "framework";

    private EventJdbcRepository jdbcRepository;
    private DataSource dataSource;

    @Before
    public void initialize() throws Exception {

        dataSource = new TestEventStoreDataSourceFactory().createDataSource("frameworkeventstore");
        jdbcRepository = new EventJdbcRepository(
                new AnsiSQLEventLogInsertionStrategy(),
                new JdbcResultSetStreamer(),
                new PreparedStatementWrapperFactory(),
                dataSource,
                mock(Logger.class));

        new DatabaseCleaner().cleanEventStoreTables(FRAMEWORK_CONTEXT_NAME);
    }

    @After
    public void after() throws SQLException {
        dataSource.getConnection().close();
    }

    @Test
    public void shouldStoreEventsUsingInsert() throws InvalidPositionException {

        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(SEQUENCE_ID).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(SEQUENCE_ID + 1).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(SEQUENCE_ID + 2).build());

        final Stream<Event> events = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);
        final Stream<Event> events2 = jdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(STREAM_ID, SEQUENCE_ID + 1);
        final Long latestSequenceId = jdbcRepository.getStreamSize(STREAM_ID);

        assertThat(events.count(), equalTo(3L));
        assertThat(events2.count(), equalTo(2L));
        assertThat(latestSequenceId, equalTo(7L));
    }

    @Test
    public void shouldReturnEventsByStreamIdOrderedBySequenceId() throws InvalidPositionException {

        jdbcRepository.insert(eventBuilder().withStreamId(randomUUID()).withSequenceId(1L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(7L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(4L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(2L).build());

        final Stream<Event> events = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(3));
        assertThat(eventList.get(0).getSequenceId(), is(2L));
        assertThat(eventList.get(1).getSequenceId(), is(4L));
        assertThat(eventList.get(2).getSequenceId(), is(7L));
    }

    @Test
    public void shouldStoreAndReturnDateCreated() throws InvalidPositionException {

        final Event event = eventBuilder().withSequenceId(1L).build();
        jdbcRepository.insert(event);

        Stream<Event> events = jdbcRepository.findByStreamIdOrderByPositionAsc(event.getStreamId());

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(1));
        assertThat(eventList.get(0).getCreatedAt(), is(event.getCreatedAt()));
    }

    @Test
    public void shouldReturnEventsByStreamIdFromSequenceIdOrderBySequenceId() throws InvalidPositionException {

        jdbcRepository.insert(eventBuilder().withStreamId(randomUUID()).withSequenceId(5L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(7L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(4L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(3L).build());

        final Stream<Event> events = jdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(STREAM_ID, 4L);
        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(2));
        assertThat(eventList.get(0).getSequenceId(), is(4L));
        assertThat(eventList.get(1).getSequenceId(), is(7L));
    }

    @Test
    public void shouldReturnEventsByStreamIdFromPostionOrderByPositionByPage() throws InvalidPositionException {

        final int pageSize = 2;

        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(7L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(4L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(3L).build());

        final Stream<Event> events = jdbcRepository.findByStreamIdFromPositionOrderByPositionAsc(STREAM_ID, 3L, pageSize);
        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(2));
        assertThat(eventList.get(0).getSequenceId(), is(3L));
        assertThat(eventList.get(1).getSequenceId(), is(4L));
    }

    @Test
    public void shouldReturnAllEventsOrderedBySequenceId() throws InvalidPositionException {

        jdbcRepository.insert(eventBuilder().withStreamId(randomUUID()).withSequenceId(1L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(4L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(2L).build());

        final Stream<Event> events = jdbcRepository.findAll();

        final List<Event> eventList = events.collect(toList());
        assertThat(eventList, hasSize(3));
        assertThat(eventList.get(0).getSequenceId(), is(1L));
        assertThat(eventList.get(1).getSequenceId(), is(2L));
        assertThat(eventList.get(2).getSequenceId(), is(4L));
    }

    @Test
    public void shouldReturnStreamOfStreamIds() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        jdbcRepository.insert(eventBuilder().withStreamId(streamId1).withSequenceId(1L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(streamId2).withSequenceId(1L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(streamId3).withSequenceId(1L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(streamId1).withSequenceId(2L)
                .build());

        final Stream<UUID> streamIds = jdbcRepository.getStreamIds();

        final List<UUID> streamIdList = streamIds.collect(toList());

        assertThat(streamIdList, hasSize(3));
        assertThat(streamIdList, hasItem(streamId1));
        assertThat(streamIdList, hasItem(streamId2));
        assertThat(streamIdList, hasItem(streamId3));
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateId() throws InvalidPositionException {
        final UUID id = randomUUID();

        jdbcRepository.insert(eventBuilder().withId(id).withSequenceId(SEQUENCE_ID).build());
        jdbcRepository.insert(eventBuilder().withId(id).withSequenceId(SEQUENCE_ID + 1).build());
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateSequenceId() throws InvalidPositionException {

        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(SEQUENCE_ID).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(SEQUENCE_ID).build());
    }

    @Test
    public void shouldClearStream() throws InvalidPositionException {

        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(1L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(2L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(3L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(4L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(5L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(6L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(7L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(8L).build());
        jdbcRepository.insert(eventBuilder().withStreamId(STREAM_ID).withSequenceId(9L).build());

        final Long latestSequenceId = jdbcRepository.getStreamSize(STREAM_ID);
        assertThat(latestSequenceId, equalTo(9L));

        jdbcRepository.clear(STREAM_ID);

        final Stream<Event> emptyEvents = jdbcRepository.findByStreamIdOrderByPositionAsc(STREAM_ID);
        assertThat(emptyEvents.count(), equalTo(0L));

        final Long deletedStreamLatestSequenceId = jdbcRepository.getStreamSize(STREAM_ID);
        assertThat(deletedStreamLatestSequenceId, equalTo(0L));
    }
}
