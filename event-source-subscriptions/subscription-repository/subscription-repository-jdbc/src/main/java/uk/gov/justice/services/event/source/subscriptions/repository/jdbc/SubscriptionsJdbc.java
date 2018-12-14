package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SubscriptionsJdbc {

    private static final String INSERT_OR_UPDATE_EVENT_NUMBER_SQL =
            "INSERT INTO subscriptions (subscription_name, current_event_number) VALUES (?, ?) " +
                    "ON CONFLICT (subscription_name) DO UPDATE " +
                    "SET current_event_number = EXCLUDED.current_event_number";
    private static final String SELECT_CURRENT_EVENT_NUMBER_SQL =
            "SELECT current_event_number " +
                    "FROM subscriptions " +
                    "WHERE subscription_name = ?";
    private static final String DELETE_CURRENT_EVENT_NUMBER_SQL =
            "DELETE FROM subscriptions " +
                    "WHERE subscription_name = ?";


    public Optional<Long> readCurrentEventNumber(final String subscriptionName, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CURRENT_EVENT_NUMBER_SQL)) {

            preparedStatement.setString(1, subscriptionName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return of(resultSet.getLong(1));
                }
            }
        }

        return empty();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void insertOrUpdateCurrentEventNumber(final long eventNumber, final String subscriptionName, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_OR_UPDATE_EVENT_NUMBER_SQL)) {
            preparedStatement.setString(1, subscriptionName);
            preparedStatement.setLong(2, eventNumber);
            preparedStatement.executeUpdate();
        }
    }

    public void deleteCurrentEventNumber(final String subscriptionName, final Connection connection) throws SQLException {

        try (final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_CURRENT_EVENT_NUMBER_SQL)) {

            preparedStatement.setString(1, subscriptionName);
            preparedStatement.executeUpdate();
        }
    }
}
