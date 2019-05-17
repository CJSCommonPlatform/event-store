package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

public class StreamStatusInserter {

    private final DataSource eventStoreDataSource;

    private final UtcClock clock = new UtcClock();

    public StreamStatusInserter(final DataSource eventStoreDataSource) {
        this.eventStoreDataSource = eventStoreDataSource;
    }

    public void insertStreamStatus(final UUID streamId) {

        final String sql = "INSERT INTO event_stream (stream_id, date_created, active) values (?, ?, ?)";
        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, streamId);
            preparedStatement.setTimestamp(2, toSqlTimestamp(clock.now()));
            preparedStatement.setBoolean(3, true);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert stream status", e);
        }
    }

    public void setStreamInactive(final UUID streamId) {

        final String sql = "UPDATE event_stream SET active = false WHERE stream_id = ?";
        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, streamId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to set stream status to inactive", e);
        }
    }
}
