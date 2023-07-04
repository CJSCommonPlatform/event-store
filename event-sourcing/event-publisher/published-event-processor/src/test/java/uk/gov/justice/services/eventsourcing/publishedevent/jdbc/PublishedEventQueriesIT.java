package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.test.utils.events.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishedEventQueriesIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    @InjectMocks
    private PublishedEventQueries publishedEventQueries;

    @BeforeEach
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
                assertThat(resultSet.getObject(3), is(publishedEvent.getPositionInStream()));
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

        final PublishedEvent publishedEvent = publishedEventBuilder()
                .withName("example.published-event")
                .withPositionInStream(1L)
                .withEventNumber(1L)
                .withPreviousEventNumber(0L)
                .build();

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

    @Test
    public void shouldLatestPublishedEvent() throws Exception {

        final PublishedEvent publishedEvent_1 = publishedEventBuilder()
                .withName("example.latest-published-event")
                .withPositionInStream(1L)
                .withEventNumber(1L)
                .withPreviousEventNumber(0L)
                .build();
        final PublishedEvent publishedEvent_2 = publishedEventBuilder()
                .withName("example.latest-published-event")
                .withPositionInStream(2L)
                .withEventNumber(2L)
                .withPreviousEventNumber(1L)
                .build();
        final PublishedEvent publishedEvent_3 = publishedEventBuilder()
                .withName("example.latest-published-event")
                .withPositionInStream(3L)
                .withEventNumber(3L)
                .withPreviousEventNumber(2L)
                .build();

        publishedEventQueries.insertPublishedEvent(publishedEvent_1, eventStoreDataSource);
        publishedEventQueries.insertPublishedEvent(publishedEvent_2, eventStoreDataSource);
        publishedEventQueries.insertPublishedEvent(publishedEvent_3, eventStoreDataSource);

        final Optional<PublishedEvent> publishedEventOptional = publishedEventQueries.getLatestPublishedEvent(eventStoreDataSource);

        if (publishedEventOptional.isPresent()) {
            assertThat(publishedEventOptional.get(), is(publishedEvent_3));
        } else {
            fail();
        }
    }

    @Test
    public void shouldReturnEmptyIfNoLatestPublishedEventFound() throws Exception {
        assertThat(publishedEventQueries.getLatestPublishedEvent(eventStoreDataSource).isEmpty(), is(true));

    }
}
