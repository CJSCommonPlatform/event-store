package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionsRepositoryIT {

    private static final String SUBSCRIPTION_NAME = "a subscription";

    private final DataSource viewStoreDataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();

    @Mock
    ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @InjectMocks
    private SubscriptionsRepository subscriptionsRepository;

    @Before
    public void cleanup() {
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        subscriptionsRepository.deleteSubscription(SUBSCRIPTION_NAME);
    }

    @Test
    public void shouldReadAndWriteTheCurrentEventNumber() throws Exception {

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        assertThat(subscriptionsRepository.startSubscription(SUBSCRIPTION_NAME), is(0L));

        final long currentEventNumber = 98234L;

        assertThat(subscriptionsRepository.getCurrentEventNumber(SUBSCRIPTION_NAME), is(not(currentEventNumber)));

        subscriptionsRepository.updateCurrentEventNumber(currentEventNumber, SUBSCRIPTION_NAME);

        assertThat(subscriptionsRepository.getCurrentEventNumber(SUBSCRIPTION_NAME), is(currentEventNumber));
    }

    @Test
    public void shouldNotWriteTheCurrentEventNumberIdNewEventNumberIsLessThanCurrentEventNumber() throws Exception {

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        assertThat(subscriptionsRepository.startSubscription(SUBSCRIPTION_NAME), is(0L));

        final long currentEventNumber = 98234L;

        subscriptionsRepository.updateCurrentEventNumber(currentEventNumber, SUBSCRIPTION_NAME);
        subscriptionsRepository.updateCurrentEventNumber(currentEventNumber - 1L, SUBSCRIPTION_NAME);

        assertThat(subscriptionsRepository.getCurrentEventNumber(SUBSCRIPTION_NAME), is(currentEventNumber));
    }

    @Test
    public void shouldDeleteSubscription() throws Exception {
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        subscriptionsRepository.startSubscription(SUBSCRIPTION_NAME);

        assertThat(subscriptionsRepository.getCurrentEventNumber(SUBSCRIPTION_NAME), is(0L));

        subscriptionsRepository.deleteSubscription(SUBSCRIPTION_NAME);

        try {
            subscriptionsRepository.getCurrentEventNumber(SUBSCRIPTION_NAME);
            fail();
        } catch (final SubscriptionRepositoryException expected) {
        }
    }
}
