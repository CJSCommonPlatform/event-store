package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

public class SubscriptionsRepository {

    private static final long SUBSCRIPTION_EVENT_NUMBER_START_VALUE = 0;

    private static final String UPDATE_EVENT_NUMBER_SQL = "UPDATE subscriptions SET current_event_number = ? WHERE subscription_name = ?";
    private static final String SELECT_CURRENT_EVENT_NUMBER_SQL = "SELECT current_event_number FROM subscriptions WHERE subscription_name = ?";
    private static final String CREATE_SUBSCRIPTION_SQL = "INSERT INTO subscriptions (subscription_name, current_event_number) VALUES (?, ?)";

    @Inject
    ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    public long startSubscription(final String subscriptionName) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection()) {
            if (hasCurrentSubscription(subscriptionName, connection)) {
                return getCurrentEventNumber(subscriptionName);
            }

            insertNewSubscription(subscriptionName, connection);

        } catch (final SQLException e) {
            throw new SubscriptionRepositoryException("Failed to insert into subscriptions table ", e);
        }

        return SUBSCRIPTION_EVENT_NUMBER_START_VALUE;
    }

    private void insertNewSubscription(final String subscriptionName, final Connection connection) throws SQLException {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(CREATE_SUBSCRIPTION_SQL)) {

            preparedStatement.setString(1, subscriptionName);
            preparedStatement.setLong(2, SUBSCRIPTION_EVENT_NUMBER_START_VALUE);
            preparedStatement.executeUpdate();
        }
    }

    public void updateCurrentEventNumber(final long eventNumber, final String subscriptionName) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_EVENT_NUMBER_SQL)) {
            preparedStatement.setLong(1, eventNumber);
            preparedStatement.setString(2, subscriptionName);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new SubscriptionRepositoryException("Failed to update current event number " + eventNumber, e);
        }
    }

    public long getCurrentEventNumber(final String subscriptionName) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CURRENT_EVENT_NUMBER_SQL)) {

            preparedStatement.setString(1, subscriptionName);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }

            throw new SubscriptionRepositoryException("Failed to find current_event_number in event_number. No rows returned");
        } catch (final SQLException e) {
            throw new SubscriptionRepositoryException("Failed to find current_event_number in event_number.", e);
        }
    }

    public void deleteSubscription(final String subscriptionName) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM subscriptions where subscription_name = ?")) {

            preparedStatement.setString(1, subscriptionName);
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new SubscriptionRepositoryException(format("Failed to delete subscription with name %s from subscriptions table.", subscriptionName), e);
        }

    }

    private boolean hasCurrentSubscription(final String subscriptionName, final Connection connection) throws SQLException {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CURRENT_EVENT_NUMBER_SQL)) {
            preparedStatement.setString(1, subscriptionName);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
