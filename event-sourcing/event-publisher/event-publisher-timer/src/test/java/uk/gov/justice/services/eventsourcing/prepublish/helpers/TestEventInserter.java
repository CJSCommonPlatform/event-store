package uk.gov.justice.services.eventsourcing.prepublish.helpers;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

public class TestEventInserter {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    private static final String INSERT_INTO_EVENT_LOG_QUERY =
            "INSERT INTO event_log (" +
                    "id, stream_id, position_in_stream, name, payload, metadata, date_created" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_INTO_LINKED_EVENT_QUERY =
            "INSERT INTO linked_event (" +
                    "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void insertIntoEventLog(final Event event) throws SQLException {
        insertIntoEventLog(
                event.getId(),
                event.getStreamId(),
                event.getSequenceId(),
                event.getCreatedAt(),
                event.getName(),
                event.getPayload(),
                event.getMetadata()

        );
    }

    public void insertIntoEventLog(
            final UUID eventLogId,
            final UUID streamId,
            final long sequenceId,
            final ZonedDateTime now,
            final String eventName,
            final JsonEnvelope jsonEnvelope
    ) throws SQLException {
        insertIntoEventLog(
                eventLogId,
                streamId,
                sequenceId,
                now,
                eventName,
                jsonEnvelope.payload().toString(),
                jsonEnvelope.metadata().asJsonObject().toString()
        );
    }

    public void insertIntoEventLog(
            final UUID eventLogId,
            final UUID streamId,
            final long sequenceId,
            final ZonedDateTime now,
            final String eventName,
            final String payload,
            final String metadata
    ) throws SQLException {
        try (final Connection connection = eventStoreDataSource.getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_EVENT_LOG_QUERY)) {
                preparedStatement.setObject(1, eventLogId);
                preparedStatement.setObject(2, streamId);
                preparedStatement.setLong(3, sequenceId);
                preparedStatement.setString(4, eventName);
                preparedStatement.setString(5, payload);
                preparedStatement.setString(6, metadata);
                preparedStatement.setObject(7, toSqlTimestamp(now));

                preparedStatement.executeUpdate();
            }
        }
    }

    public void insertIntoLinkedEvent(final LinkedEvent linkedEvent) throws SQLException {

        try (final Connection connection = eventStoreDataSource.getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_LINKED_EVENT_QUERY)) {
                preparedStatement.setObject(1, linkedEvent.getId());
                preparedStatement.setObject(2, linkedEvent.getStreamId());
                preparedStatement.setLong(3, linkedEvent.getSequenceId());
                preparedStatement.setString(4, linkedEvent.getName());
                preparedStatement.setString(5, linkedEvent.getPayload());
                preparedStatement.setString(6, linkedEvent.getMetadata());
                preparedStatement.setObject(7, toSqlTimestamp(linkedEvent.getCreatedAt()));
                preparedStatement.setLong(8, linkedEvent.getEventNumber().get());
                preparedStatement.setLong(9, linkedEvent.getPreviousEventNumber());

                preparedStatement.executeUpdate();
            }
        }
    }
}
