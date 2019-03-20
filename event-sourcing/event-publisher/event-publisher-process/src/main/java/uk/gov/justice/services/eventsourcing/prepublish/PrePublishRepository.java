package uk.gov.justice.services.eventsourcing.prepublish;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.PublishQueueException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PrePublishRepository {

    private static final int NO_PREVIOUS_EVENT_NUMBER = 0;
    private static final String SELECT_EVENT_NUMBER_SQL = "SELECT event_number FROM event_log WHERE id = ?";
    private static final String SELECT_PREVIOUS_EVENT_NUMBER_SQL = "SELECT event_number FROM event_log, event_stream WHERE event_number < ? and event_log.stream_id = event_stream.stream_id and  event_stream.active = true ORDER BY event_number DESC LIMIT 1";
    private static final String INSERT_INTO_PUBLISH_QUEUE_SQL = "INSERT INTO publish_queue (event_log_id, date_queued) VALUES (?, ?)";
    private static final String INSERT_INTO_LINKED_EVENT_SQL = "INSERT into linked_event (" +
            "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public long getEventNumber(final UUID eventId, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_EVENT_NUMBER_SQL)) {
            preparedStatement.setObject(1, eventId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong("event_number");
                }

                throw new PublishQueueException("Failed to get event_number from event_log table");
            }
        }
    }

    public long getPreviousEventNumber(final long sequenceId, final Connection connection) throws SQLException {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PREVIOUS_EVENT_NUMBER_SQL)) {
            preparedStatement.setLong(1, sequenceId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong("event_number");
                }

                return NO_PREVIOUS_EVENT_NUMBER;
            }
        }
    }

    public void insertLinkedEvent(final LinkedEvent linkedEvent, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_LINKED_EVENT_SQL)) {
            preparedStatement.setObject(1, linkedEvent.getId());
            preparedStatement.setObject(2, linkedEvent.getStreamId());
            preparedStatement.setLong(3, linkedEvent.getSequenceId());
            preparedStatement.setString(4, linkedEvent.getName());
            preparedStatement.setString(5, linkedEvent.getPayload());
            preparedStatement.setString(6, linkedEvent.getMetadata());
            preparedStatement.setObject(7, toSqlTimestamp(linkedEvent.getCreatedAt()));
            preparedStatement.setLong(8, linkedEvent.getEventNumber().orElseThrow(() -> new MissingEventNumberException("Event with id '%s' does not have an event number")));
            preparedStatement.setLong(9, linkedEvent.getPreviousEventNumber());

            preparedStatement.execute();
        }
    }

    public void addToPublishQueueTable(final UUID eventId, final ZonedDateTime now, final Connection connection) throws SQLException {

        try(final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_PUBLISH_QUEUE_SQL)) {
            preparedStatement.setObject(1, eventId);
            preparedStatement.setTimestamp(2, toSqlTimestamp(now));

            preparedStatement.executeUpdate();
        }
    }
}
