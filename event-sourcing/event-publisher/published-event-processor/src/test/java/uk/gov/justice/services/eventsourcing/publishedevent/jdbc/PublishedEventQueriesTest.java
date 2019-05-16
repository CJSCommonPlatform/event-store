package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.publish.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventQueriesTest {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final EventFactory eventFactory = new EventFactory();

    @InjectMocks
    private PublishedEventQueries publishedEventQueries;

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

        publishedEventQueries.insertPublishedEvent(publishedEvent, eventStoreDataSource);

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
    public void shouldFetchPublishedEventById() throws Exception {

        final PublishedEvent publishedEvent = eventFactory.createPublishedEvent(randomUUID(), "example.published-event", 1L, 1L, 0L);

        publishedEventQueries.insertPublishedEvent(publishedEvent, eventStoreDataSource);

        final Optional<PublishedEvent> publishedEventOptional = publishedEventQueries.getPublishedEvent(
                publishedEvent.getId(),
                eventStoreDataSource);

        if (publishedEventOptional.isPresent()) {
            assertThat(publishedEventOptional.get(), is(publishedEvent));
        } else {
            fail();
        }
    }

    @Test
    public void shouldReturnEmptyIfNoPublishedEventFound() throws Exception {

        final UUID unknownId = randomUUID();

        assertThat(publishedEventQueries.getPublishedEvent(unknownId, eventStoreDataSource).isPresent(), is(false));
    }

    @Test
    public void shouldTruncatePublishedEventTable() throws Exception {

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

        publishedEventQueries.insertPublishedEvent(publishedEvent, eventStoreDataSource);
        publishedEventQueries.truncate(eventStoreDataSource);


        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM published_event");
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                fail();
            }
        }
    }
}
