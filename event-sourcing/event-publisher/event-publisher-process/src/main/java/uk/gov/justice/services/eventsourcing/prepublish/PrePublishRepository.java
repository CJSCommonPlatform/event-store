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

    private static final int NO_PREVIOUS_SEQUENCE_NUMBER = 0;
    private static final String SELECT_SEQUENCE_NUMBER_SQL = "SELECT sequence_number FROM event_log WHERE id = ?";
    private static final String SELECT_PREVIOUS_SEQUENCE_NUMBER_SQL = "SELECT sequence_number FROM event_log WHERE sequence_number < ? ORDER BY sequence_number DESC LIMIT 1";
    private static final String UPDATE_METADATA_SQL = "UPDATE event_log SET metadata = ? where id = ?";
    private static final String INSERT_INTO_PUBLISH_QUEUE_SQL = "INSERT INTO publish_queue (event_log_id, date_queued) VALUES (?, ?)";

    public long getSequenceNumber(final UUID eventId, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SEQUENCE_NUMBER_SQL)) {
            preparedStatement.setObject(1, eventId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong("sequence_number");
                }

                throw new PublishQueueException("Failed to get sequence_number from event_log table");
            }
        }
    }

    public long getPreviousSequenceNumber(final long sequenceId, final Connection connection) throws SQLException {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PREVIOUS_SEQUENCE_NUMBER_SQL)) {
            preparedStatement.setLong(1, sequenceId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong("sequence_number");
                }

                return NO_PREVIOUS_SEQUENCE_NUMBER;
            }
        }
    }

    public void updateMetadata(final UUID eventId, final String metadataJson, final Connection connection) throws SQLException {

        try(final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_METADATA_SQL)) {
            preparedStatement.setString(1, metadataJson);
            preparedStatement.setObject(2, eventId);

            preparedStatement.executeUpdate();
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
