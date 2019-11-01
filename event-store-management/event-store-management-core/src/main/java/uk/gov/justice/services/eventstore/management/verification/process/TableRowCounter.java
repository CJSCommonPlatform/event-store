package uk.gov.justice.services.eventstore.management.verification.process;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class TableRowCounter {

    public int countRowsIn(final String tableName, final DataSource dataSource) {

        final String query = format("SELECT count(*) FROM %s", tableName);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(query);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            throw new CatchupVerificationException(format("Counting rows in %s table did not return any results", tableName));

        } catch (SQLException e) {
            throw new CatchupVerificationException(format("Failed to count rows in %s table", tableName), e);
        }
    }
}
