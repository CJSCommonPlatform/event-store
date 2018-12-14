package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class SubscriptionsRepository {

    private static final long SUBSCRIPTION_EVENT_NUMBER_START_VALUE = 0;

    @Inject
    ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Inject
    SubscriptionsJdbc subscriptionsJdbc;


    @Transactional(REQUIRED)
    public void insertOrUpdateCurrentEventNumber(final long eventNumber, final String subscriptionName) {
        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection()) {
            subscriptionsJdbc.insertOrUpdateCurrentEventNumber(eventNumber, subscriptionName, connection);
        } catch (final SQLException e) {
            throw new SubscriptionRepositoryException(format("Failed to update current_event_number to %d in subscriptions table.", eventNumber), e);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Transactional(REQUIRED)
    public long getOrInitialiseCurrentEventNumber(final String subscriptionName) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection()) {

            final Optional<Long> currentEventNumber = subscriptionsJdbc.readCurrentEventNumber(
                    subscriptionName,
                    connection);

            if (currentEventNumber.isPresent()) {
                return currentEventNumber.get();
            }

            subscriptionsJdbc.insertOrUpdateCurrentEventNumber(
                    SUBSCRIPTION_EVENT_NUMBER_START_VALUE,
                    subscriptionName,
                    connection);

            return subscriptionsJdbc.readCurrentEventNumber(subscriptionName, connection).get();
            
        } catch (final SQLException e) {
            throw new SubscriptionRepositoryException("Failed to get current_event_number from subscriptions table.", e);
        }
    }
}
