package uk.gov.justice.services.eventstore.management.catchup.state;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupStateManagerTest {

    @InjectMocks
    private CatchupStateManager catchupStateManager;

    @Test
    public void shouldMaintainACacheOfAllCatchupsInProgress() throws Exception {

        assertThat(catchupStateManager.noCatchupsInProgress(), is(true));

        final SubscriptionCatchupDetails subscriptionCatchupDetails_1 = mock(SubscriptionCatchupDetails.class);
        final SubscriptionCatchupDetails subscriptionCatchupDetails_2 = mock(SubscriptionCatchupDetails.class);

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetailsList = asList(
                subscriptionCatchupDetails_1,
                subscriptionCatchupDetails_2
        );

        final ZonedDateTime catchupStartedAt = new UtcClock().now();

        catchupStateManager.newCatchupInProgress(subscriptionCatchupDetailsList, catchupStartedAt);

        assertThat(catchupStateManager.isCatchupInProgress(subscriptionCatchupDetails_1), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(subscriptionCatchupDetails_2), is(true));

        assertThat(catchupStateManager.noCatchupsInProgress(), is(false));
    }

    @Test
    public void shouldRemoveCatchupsInProgress() throws Exception {

        assertThat(catchupStateManager.noCatchupsInProgress(), is(true));

        final SubscriptionCatchupDetails subscriptionCatchupDetails = mock(SubscriptionCatchupDetails.class);

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetailsList = singletonList(subscriptionCatchupDetails);

        final ZonedDateTime catchupStartedAt = new UtcClock().now();

        catchupStateManager.newCatchupInProgress(subscriptionCatchupDetailsList, catchupStartedAt);

        assertThat(catchupStateManager.isCatchupInProgress(subscriptionCatchupDetails), is(true));
        assertThat(catchupStateManager.noCatchupsInProgress(), is(false));

        final CatchupInProgress catchupInProgress = catchupStateManager.removeCatchupInProgress(subscriptionCatchupDetails);

        assertThat(catchupInProgress.getSubscriptionCatchupDetails(), is(subscriptionCatchupDetails));
        assertThat(catchupInProgress.getStartedAt(), is(catchupStartedAt));

        assertThat(catchupStateManager.isCatchupInProgress(subscriptionCatchupDetails), is(false));
        assertThat(catchupStateManager.noCatchupsInProgress(), is(true));
    }

    @Test
    public void shouldClearCatchups() throws Exception {

        assertThat(catchupStateManager.noCatchupsInProgress(), is(true));

        final SubscriptionCatchupDetails subscriptionCatchupDetails = mock(SubscriptionCatchupDetails.class);

        final List<SubscriptionCatchupDetails> subscriptionCatchupDetailsList = singletonList(subscriptionCatchupDetails);

        final ZonedDateTime catchupStartedAt = new UtcClock().now();

        catchupStateManager.newCatchupInProgress(subscriptionCatchupDetailsList, catchupStartedAt);

        assertThat(catchupStateManager.isCatchupInProgress(subscriptionCatchupDetails), is(true));
        assertThat(catchupStateManager.noCatchupsInProgress(), is(false));

        catchupStateManager.clear();

        assertThat(catchupStateManager.isCatchupInProgress(subscriptionCatchupDetails), is(false));
        assertThat(catchupStateManager.noCatchupsInProgress(), is(true));

    }
}
