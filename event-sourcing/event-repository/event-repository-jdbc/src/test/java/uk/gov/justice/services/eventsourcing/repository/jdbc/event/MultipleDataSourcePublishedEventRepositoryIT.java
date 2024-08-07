package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.test.utils.events.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultipleDataSourcePublishedEventRepositoryIT {

    private DataSource dataSource;

    private MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository;

    @BeforeEach
    public void initialize() throws Exception {
        dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
        new DatabaseCleaner().cleanEventStoreTables("framework");

        final JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();
        final PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();
        multipleDataSourcePublishedEventRepository = new MultipleDataSourcePublishedEventRepository(
                jdbcResultSetStreamer,
                preparedStatementWrapperFactory,
                dataSource);
    }

    @AfterEach
    public void after() throws SQLException {
        dataSource.getConnection().close();
    }

    @Test
    public void shouldGetEventsSinceEventNumber() throws Exception {

        final PublishedEvent event_1 = publishedEventBuilder().withPreviousEventNumber(0).withEventNumber(1).build();
        final PublishedEvent event_2 = publishedEventBuilder().withPreviousEventNumber(1).withEventNumber(2).build();
        final PublishedEvent event_3 = publishedEventBuilder().withPreviousEventNumber(2).withEventNumber(3).build();
        final PublishedEvent event_4 = publishedEventBuilder().withPreviousEventNumber(3).withEventNumber(4).build();
        final PublishedEvent event_5 = publishedEventBuilder().withPreviousEventNumber(4).withEventNumber(5).build();

        final Connection connection = dataSource.getConnection();

        insertPublishedEvent(event_1, connection);
        insertPublishedEvent(event_2, connection);
        insertPublishedEvent(event_3, connection);
        insertPublishedEvent(event_4, connection);
        insertPublishedEvent(event_5, connection);

        final List<PublishedEvent> publishedEvents = multipleDataSourcePublishedEventRepository.findEventsSince(3)
                .collect(toList());

        assertThat(publishedEvents.size(), is(2));

        assertThat(publishedEvents.get(0).getId(), is(event_4.getId()));
        assertThat(publishedEvents.get(1).getId(), is(event_5.getId()));
    }

    @Test
    public void shouldGetEventRange() throws Exception {

        final PublishedEvent event_1 = publishedEventBuilder().withPreviousEventNumber(0).withEventNumber(1).build();
        final PublishedEvent event_2 = publishedEventBuilder().withPreviousEventNumber(1).withEventNumber(2).build();
        final PublishedEvent event_3 = publishedEventBuilder().withPreviousEventNumber(2).withEventNumber(3).build();
        final PublishedEvent event_4 = publishedEventBuilder().withPreviousEventNumber(3).withEventNumber(4).build();
        final PublishedEvent event_5 = publishedEventBuilder().withPreviousEventNumber(4).withEventNumber(5).build();

        final Connection connection = dataSource.getConnection();

        insertPublishedEvent(event_1, connection);
        insertPublishedEvent(event_2, connection);
        insertPublishedEvent(event_3, connection);
        insertPublishedEvent(event_4, connection);
        insertPublishedEvent(event_5, connection);

        final List<PublishedEvent> publishedEvents = multipleDataSourcePublishedEventRepository.findEventRange(1, 4)
                .collect(toList());

        assertThat(publishedEvents.size(), is(3));

        assertThat(publishedEvents.get(0).getId(), is(event_1.getId()));
        assertThat(publishedEvents.get(1).getId(), is(event_2.getId()));
        assertThat(publishedEvents.get(2).getId(), is(event_3.getId()));
    }

    @Test
    public void fetchByEventIdShouldReturnEventIfExists() throws Exception {
        final Connection connection = dataSource.getConnection();
        final PublishedEvent event = publishedEventBuilder().withPreviousEventNumber(0).withEventNumber(1).build();
        insertPublishedEvent(event, connection);

        final Optional<PublishedEvent> fetchedEvent = multipleDataSourcePublishedEventRepository.findByEventId(event.getId());

        assertTrue(fetchedEvent.isPresent());
        assertThat(fetchedEvent.get().getId(), is(event.getId()));
    }

    @Test
    public void fetchByEventIdShouldReturnEmptyIfEventNotExist() throws Exception {
        final Optional<PublishedEvent> fetchedEvent = multipleDataSourcePublishedEventRepository.findByEventId(UUID.randomUUID());

        assertFalse(fetchedEvent.isPresent());
    }

    @Test
    public void shouldGetLatestPublishedEvent() throws Exception {

        final PublishedEvent event_1 = publishedEventBuilder().withPreviousEventNumber(0).withEventNumber(1).build();
        final PublishedEvent event_2 = publishedEventBuilder().withPreviousEventNumber(1).withEventNumber(2).build();
        final PublishedEvent event_3 = publishedEventBuilder().withPreviousEventNumber(2).withEventNumber(3).build();
        final PublishedEvent event_4 = publishedEventBuilder().withPreviousEventNumber(3).withEventNumber(4).build();
        final PublishedEvent event_5 = publishedEventBuilder().withPreviousEventNumber(4).withEventNumber(5).build();

        final Connection connection = dataSource.getConnection();
        
        insertPublishedEvent(event_1, connection);
        insertPublishedEvent(event_2, connection);
        insertPublishedEvent(event_3, connection);
        insertPublishedEvent(event_4, connection);
        insertPublishedEvent(event_5, connection);

        final Optional<PublishedEvent> latestPublishedEvent = multipleDataSourcePublishedEventRepository.getLatestPublishedEvent();

        if (latestPublishedEvent.isPresent()) {
            assertThat(latestPublishedEvent.get().getId(), is(event_5.getId()));
            assertThat(latestPublishedEvent.get().getName(), is(event_5.getName()));
            assertThat(latestPublishedEvent.get().getStreamId(), is(event_5.getStreamId()));
            assertThat(latestPublishedEvent.get().getMetadata(), is(event_5.getMetadata()));
            assertThat(latestPublishedEvent.get().getCreatedAt(), is(event_5.getCreatedAt()));
            assertThat(latestPublishedEvent.get().getEventNumber(), is(event_5.getEventNumber()));
            assertThat(latestPublishedEvent.get().getPreviousEventNumber(), is(event_5.getPreviousEventNumber()));
            assertThat(latestPublishedEvent.get().getPositionInStream(), is(event_5.getPositionInStream()));
            assertThat(latestPublishedEvent.get().getPayload(), is(event_5.getPayload()));
        } else {
            fail();
        }
    }

    @Test
    public void shouldReturnEmptyWhenGettingLatestPublishedEventIfNoPublishedEventsExist() throws Exception {
        assertThat(multipleDataSourcePublishedEventRepository.getLatestPublishedEvent(), is(empty()));
    }

    private void insertPublishedEvent(final PublishedEvent publishedEvent, final Connection connection) throws SQLException {

        final String sql = "INSERT into published_event (" +
                "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, publishedEvent.getId());
            preparedStatement.setObject(2, publishedEvent.getStreamId());
            preparedStatement.setLong(3, publishedEvent.getPositionInStream());
            preparedStatement.setString(4, publishedEvent.getName());
            preparedStatement.setString(5, publishedEvent.getPayload());
            preparedStatement.setString(6, publishedEvent.getMetadata());
            preparedStatement.setObject(7, toSqlTimestamp(publishedEvent.getCreatedAt()));
            preparedStatement.setLong(8, publishedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException("Event with id '%s' does not have an event number")));
            preparedStatement.setLong(9, publishedEvent.getPreviousEventNumber());

            preparedStatement.execute();
        }
    }
}
