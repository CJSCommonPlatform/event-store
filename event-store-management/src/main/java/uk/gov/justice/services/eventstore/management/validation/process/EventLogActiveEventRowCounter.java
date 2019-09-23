package uk.gov.justice.services.eventstore.management.validation.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class EventLogActiveEventRowCounter {

    public int getActiveEventCountFromEventLog(final DataSource eventStoreDataSource) {

        final String query =
                "SELECT count(*) " +
                        "FROM event_log " +
                        "INNER JOIN event_stream " +
                        "ON event_stream.stream_id = event_log.stream_id " +
                        "WHERE event_stream.active = 'True'";


        try (final Connection connection = eventStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            throw new CatchupVerificationException("COUNT(*) query returned no results");
        } catch (SQLException e) {
            throw new CatchupVerificationException("Failed to get count of active events in event_log table", e);
        }
    }
}
