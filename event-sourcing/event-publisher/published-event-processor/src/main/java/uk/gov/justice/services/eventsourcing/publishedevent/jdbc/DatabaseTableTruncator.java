package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DatabaseTableTruncator {

    public void truncate(final String tableName, final DataSource dataSource) throws SQLException {

        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE " + tableName)) {
                preparedStatement.executeUpdate();
            }
        }
    }
}
