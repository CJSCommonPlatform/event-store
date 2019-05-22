package uk.gov.justice.services.eventsourcing.publishedevent.publish;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.events.TestEventInserter;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class EventLogPublishIT {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final TestEventInserter testEventInserter = new TestEventInserter(eventStoreDataSource);

    private final UtcClock utcClock = new UtcClock();

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(eventStoreDataSource);
    }

    @Test
    public void shouldUpdateThePublishQueueTableIfARowIsInsertedIntoTheEventLogTable() throws Exception {

        final UUID eventLogId = randomUUID();
        final UUID streamId = randomUUID();

        final ZonedDateTime now = utcClock.now();

        final String eventName = "my-context.some-event-or-other";
        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId),
                createObjectBuilder().add("some-property-name", "the value")
        );

        final Event event = eventBuilder()
                .withId(eventLogId)
                .withName(eventName)
                .withStreamId(streamId)
                .withTimestamp(now)
                .withMetadataJSON(jsonEnvelope.metadata().asJsonObject().toString())
                .withPayloadJSON(jsonEnvelope.payloadAsJsonObject().toString())
                .build();

        testEventInserter.insertIntoEventLog(event);

        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM pre_publish_queue");
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String foundEventLogId = resultSet.getString("event_log_id");
                final ZonedDateTime dateQueued = fromSqlTimestamp(resultSet.getTimestamp("date_queued"));

                assertThat(resultSet.next(), is(false));
                assertThat(id, is(greaterThan(0)));
                assertThat(foundEventLogId, is(eventLogId.toString()));
                assertThat(dateQueued, is(notNullValue()));
            } else {
                fail();
            }
        }
    }
}
