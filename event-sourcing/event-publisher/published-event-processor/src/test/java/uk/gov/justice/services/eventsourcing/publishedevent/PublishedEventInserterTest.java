package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventInserterTest {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    @InjectMocks
    private PublishedEventInserter publishedEventInserter;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
    }

    @Test
    public void shouldInsertPublishedEvent() throws Exception {

        final PublishedEvent publishedEvent = new PublishedEvent(
                randomUUID(),
                randomUUID(),
                982347L,
                "an-event.name",
                "{\"some\": \"metadata\"}",
                "{\"the\": \"payload\"}",
                new UtcClock().now(),
                23L,
                22L
        );

        try (final Connection connection = eventStoreDataSource.getConnection()) {
            publishedEventInserter.insertPublishedEvent(publishedEvent, connection);
        }

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM published_event");
             final ResultSet resultSet = preparedStatement.executeQuery()) {


            if (resultSet.next()) {
                assertThat(resultSet.getObject(1), is(publishedEvent.getId()));
                assertThat(resultSet.getObject(2), is(publishedEvent.getStreamId()));
                assertThat(resultSet.getObject(3), is(publishedEvent.getSequenceId()));
                assertThat(resultSet.getString(4), is(publishedEvent.getName()));
                assertThat(resultSet.getString(5), is(publishedEvent.getPayload()));
                assertThat(resultSet.getString(6), is(publishedEvent.getMetadata()));
                assertThat(fromSqlTimestamp(resultSet.getTimestamp(7)), is(publishedEvent.getCreatedAt()));
                assertThat(resultSet.getLong(8), is(publishedEvent.getEventNumber().get()));
                assertThat(resultSet.getObject(9), is(publishedEvent.getPreviousEventNumber()));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldTrucatePublishedEventTable() throws Exception {

        final PublishedEvent publishedEvent = new PublishedEvent(
                randomUUID(),
                randomUUID(),
                982347L,
                "an-event.name",
                "{\"some\": \"metadata\"}",
                "{\"the\": \"payload\"}",
                new UtcClock().now(),
                23L,
                22L
        );

        try (final Connection connection = eventStoreDataSource.getConnection()) {
            publishedEventInserter.insertPublishedEvent(publishedEvent, connection);
        }
        try (final Connection connection = eventStoreDataSource.getConnection()) {
            publishedEventInserter.truncate(connection);
        }

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM published_event");
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                fail();
            }
        }
    }
}
