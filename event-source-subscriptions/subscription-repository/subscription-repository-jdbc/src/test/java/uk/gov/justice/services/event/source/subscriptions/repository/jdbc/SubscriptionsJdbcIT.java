package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class SubscriptionsJdbcIT {

    private static final String SUBSCRIPTION_NAME = "a subscription";

    private final DataSource viewStoreDataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();

    private final SubscriptionsJdbc subscriptionsJdbc = new SubscriptionsJdbc();

    @Before
    public void cleanup() throws Exception {
        try (final Connection connection = viewStoreDataSource.getConnection()){
            subscriptionsJdbc.deleteCurrentEventNumber(SUBSCRIPTION_NAME, connection);
        }
    }

    @Test
    public void shouldUpdateAndReadCurrentEventNumber() throws Exception {

        final long eventNumber = 23L;

        try (final Connection connection = viewStoreDataSource.getConnection()){

            assertThat(subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection).isPresent(), is(false));

            subscriptionsJdbc.insertOrUpdateCurrentEventNumber(eventNumber, SUBSCRIPTION_NAME, connection);

            final Optional<Long> currentEventNumber = subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection);

            if (currentEventNumber.isPresent()) {
                assertThat(currentEventNumber.get(), is(eventNumber));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldInsertOrUpdateCurrentEventNumber() throws Exception {

        final long firstEventNumber = 238467;
        final long secondEventNumber = 9823472;
        try (final Connection connection = viewStoreDataSource.getConnection()){

            assertThat(subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection).isPresent(), is(false));

            subscriptionsJdbc.insertOrUpdateCurrentEventNumber(
                    firstEventNumber,
                    SUBSCRIPTION_NAME,
                    connection);

            assertThat(subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection).get(), is(firstEventNumber));

            subscriptionsJdbc.insertOrUpdateCurrentEventNumber(
                    secondEventNumber,
                    SUBSCRIPTION_NAME,
                    connection);

            assertThat(subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection).get(), is(secondEventNumber));
        }
    }

    @Test
    public void shouldReturnEmptyOnGetCurrentNumberIfCurrentNumberDoesNotExistInDatabase() throws Exception {

        try (final Connection connection = viewStoreDataSource.getConnection()){
            assertThat(subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection).isPresent(), is(false));
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void shouldDeleteCurrentEventNumber() throws Exception {
        try (final Connection connection = viewStoreDataSource.getConnection()){

            final long eventNumber = 7234L;
            subscriptionsJdbc.insertOrUpdateCurrentEventNumber(
                    eventNumber,
                    SUBSCRIPTION_NAME,
                    connection);

            assertThat(subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection).get(), is(eventNumber));


            subscriptionsJdbc.deleteCurrentEventNumber(SUBSCRIPTION_NAME, connection);

            final Optional<Long> currentEventNumber = subscriptionsJdbc.readCurrentEventNumber(SUBSCRIPTION_NAME, connection);

            assertThat(currentEventNumber.isPresent(), is(false));
        }
    }
}
