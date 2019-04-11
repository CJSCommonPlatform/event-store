package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
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
public class LinkedEventJdbcRepositoryTest {
    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    @InjectMocks
    LinkedEventJdbcRepository linkedEventJdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
    }

    @Test
    public void shouldInsertALinkedEvent() throws Exception {

        final LinkedEvent linkedEvent = new LinkedEvent(
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
            linkedEventJdbcRepository.insertLinkedEvent(linkedEvent, connection);
        }

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM linked_event");
             final ResultSet resultSet = preparedStatement.executeQuery()) {


            if (resultSet.next()) {
                assertThat(resultSet.getObject(1), is(linkedEvent.getId()));
                assertThat(resultSet.getObject(2), is(linkedEvent.getStreamId()));
                assertThat(resultSet.getObject(3), is(linkedEvent.getSequenceId()));
                assertThat(resultSet.getString(4), is(linkedEvent.getName()));
                assertThat(resultSet.getString(5), is(linkedEvent.getPayload()));
                assertThat(resultSet.getString(6), is(linkedEvent.getMetadata()));
                assertThat(fromSqlTimestamp(resultSet.getTimestamp(7)), is(linkedEvent.getCreatedAt()));
                assertThat(resultSet.getLong(8), is(linkedEvent.getEventNumber().get()));
                assertThat(resultSet.getObject(9), is(linkedEvent.getPreviousEventNumber()));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldTrucateLinkedEventTable() throws Exception {

        final LinkedEvent linkedEvent = new LinkedEvent(
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
            linkedEventJdbcRepository.insertLinkedEvent(linkedEvent, connection);
        }
        try (final Connection connection = eventStoreDataSource.getConnection()) {
            linkedEventJdbcRepository.truncate(connection);
        }

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM linked_event");
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                fail();
            }
        }
    }
}
