package uk.gov.justice.services.eventsourcing.util.sql.triggers;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

public class DatabaseTriggerManipulator {

    private final DataSource dataSource;

    public DatabaseTriggerManipulator(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void removeTriggerFromTable(final String triggerName, final String tableName) {

        executeStatement(format("DROP TRIGGER %s on %s CASCADE", triggerName, tableName));
    }


    public void addInsertTriggerToTable(final String triggerName, final String tableName, final String action) {

        final String statement = format("CREATE TRIGGER %s AFTER INSERT ON %s FOR EACH ROW %s", triggerName, tableName, action);

        executeStatement(statement);
    }

    public List<TriggerData> listTriggersOnTable(final String tableName) {

        final String statement =
                "SELECT " +
                        "trigger_name, " +
                        "event_manipulation, " +
                        "action_statement, " +
                        "action_timing " +
                        "FROM  information_schema.triggers " +
                        "WHERE event_object_table = ? " +
                        "ORDER BY event_object_table";

        final List<TriggerData> triggerDataList = new ArrayList<>();

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(statement)) {

            preparedStatement.setString(1, tableName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    final String triggerName = resultSet.getString("trigger_name");
                    final String manipulationType = resultSet.getString("event_manipulation");
                    final String action = resultSet.getString("action_statement");
                    final String timing = resultSet.getString("action_timing");

                    final TriggerData triggerData = new TriggerData(
                            tableName,
                            triggerName,
                            manipulationType,
                            action,
                            timing
                    );

                    triggerDataList.add(triggerData);
                }

            }

        } catch (final SQLException e) {
            throw new TriggerManipulationFailedException(format("Failed to list triggers on %s table", tableName), e);
        }

        return triggerDataList;
    }

    public Optional<TriggerData> findTriggerOnTable(final String triggerName, final String tableName) {

        final String statement =
                "SELECT " +
                        "event_object_table, " +
                        "event_manipulation, " +
                        "action_statement, " +
                        "action_timing " +
                        "FROM  information_schema.triggers " +
                        "WHERE event_object_table = ? " +
                        "AND trigger_name = ? " +
                        "ORDER BY event_object_table";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(statement)) {

            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, triggerName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    final String table = resultSet.getString("event_object_table");
                    final String manipulationType = resultSet.getString("event_manipulation");
                    final String action = resultSet.getString("action_statement");
                    final String timing = resultSet.getString("action_timing");

                    final TriggerData triggerData = new TriggerData(
                            table,
                            triggerName,
                            manipulationType,
                            action,
                            timing
                    );

                    return of(triggerData);
                }
            }

        } catch (final SQLException e) {
            throw new TriggerManipulationFailedException(format("Failed to find trigger on %s table", tableName), e);
        }

        return empty();
    }

    private void executeStatement(final String statement) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(statement)) {

            preparedStatement.execute();

        } catch (final SQLException e) {
            throw new TriggerManipulationFailedException(format("Failed to run statement '%s'", statement), e);
        }

    }
}
