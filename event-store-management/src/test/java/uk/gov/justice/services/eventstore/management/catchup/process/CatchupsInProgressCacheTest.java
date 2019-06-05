package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class CatchupsInProgressCacheTest {

    @InjectMocks
    private CatchupsInProgressCache catchupsInProgressCache;

    @Test
    public void shouldMaintainACacheOfAllCatchupsInProgress() throws Exception {

         assertThat(catchupsInProgressCache.getAllCatchupsInProgress().isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress("subscription_1", startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress("subscription_2", startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress("subscription_3", startedAt.plusMinutes(2));

        catchupsInProgressCache.addCatchupInProgress(catchupInProgress_1);
        catchupsInProgressCache.addCatchupInProgress(catchupInProgress_2);
        catchupsInProgressCache.addCatchupInProgress(catchupInProgress_3);

        assertThat(catchupsInProgressCache.isCatchupInProgress(catchupInProgress_1.getSubscriptionName()), is(true));
        assertThat(catchupsInProgressCache.isCatchupInProgress(catchupInProgress_2.getSubscriptionName()), is(true));
        assertThat(catchupsInProgressCache.isCatchupInProgress(catchupInProgress_3.getSubscriptionName()), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupsInProgressCache.getAllCatchupsInProgress();

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupsInProgressCache.removeCatchupInProgress(catchupInProgress_2.getSubscriptionName());

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupsInProgressCache.isCatchupInProgress(catchupInProgress_1.getSubscriptionName()), is(true));
        assertThat(catchupsInProgressCache.isCatchupInProgress(catchupInProgress_2.getSubscriptionName()), is(false));
        assertThat(catchupsInProgressCache.isCatchupInProgress(catchupInProgress_3.getSubscriptionName()), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupsInProgressCache.getAllCatchupsInProgress();

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupsInProgressCache.removeAll();

        assertThat(catchupsInProgressCache.noCatchupsInProgress(), is(true));
        assertThat(catchupsInProgressCache.getAllCatchupsInProgress().isEmpty(), is(true));
    }
}
