package uk.gov.justice.services.eventsourcing.prepublish;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.eventsourcing.PublishQueueException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

public class PrePublishRepository {

    private static final int NO_PREVIOUS_EVENT_NUMBER = 0;
    private static final String SELECT_EVENT_NUMBER_SQL = "SELECT event_number FROM event_log WHERE id = ?";
    private static final String SELECT_PREVIOUS_EVENT_NUMBER_SQL = "SELECT event_number FROM event_log WHERE event_number < ? ORDER BY event_number DESC LIMIT 1";
    private static final String INSERT_INTO_PUBLISH_QUEUE_SQL = "INSERT INTO publish_queue (event_log_id, date_queued) VALUES (?, ?)";

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

    public void addToPublishQueueTable(final UUID eventId, final ZonedDateTime now, final Connection connection) throws SQLException {

        try(final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_PUBLISH_QUEUE_SQL)) {
            preparedStatement.setObject(1, eventId);
            preparedStatement.setTimestamp(2, toSqlTimestamp(now));

            preparedStatement.executeUpdate();
        }
    }
}
