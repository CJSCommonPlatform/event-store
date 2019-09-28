package uk.gov.justice.services.eventstore.management.catchup.state;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;

import java.time.ZonedDateTime;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupStateManagerTest {

    @InjectMocks
    private CatchupStateManager catchupStateManager;

    @Test
    public void shouldMaintainACacheOfAllEventCatchupsInProgress() throws Exception {

        final CatchupInProgress indexCatchupInProgress = mock(CatchupInProgress.class);
        when(indexCatchupInProgress.getSubscriptionName()).thenReturn("different_catchup");

        catchupStateManager.addCatchupInProgress(indexCatchupInProgress, INDEX_CATCHUP);

         assertThat(catchupStateManager.getAllCatchupsInProgress(EVENT_CATCHUP).isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress("subscription_1", startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress("subscription_2", startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress("subscription_3", startedAt.plusMinutes(2));

        catchupStateManager.addCatchupInProgress(catchupInProgress_1, EVENT_CATCHUP);
        catchupStateManager.addCatchupInProgress(catchupInProgress_2, EVENT_CATCHUP);
        catchupStateManager.addCatchupInProgress(catchupInProgress_3, EVENT_CATCHUP);

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), EVENT_CATCHUP), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), EVENT_CATCHUP), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), EVENT_CATCHUP), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(EVENT_CATCHUP);

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupStateManager.removeCatchupInProgress(catchupInProgress_2.getSubscriptionName(), EVENT_CATCHUP);

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), EVENT_CATCHUP), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), EVENT_CATCHUP), is(false));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), EVENT_CATCHUP), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(EVENT_CATCHUP);

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupStateManager.clear(EVENT_CATCHUP);

        assertThat(catchupStateManager.noCatchupsInProgress(EVENT_CATCHUP), is(true));
        assertThat(catchupStateManager.getAllCatchupsInProgress(EVENT_CATCHUP).isEmpty(), is(true));
    }

    @Test
    public void shouldMaintainACacheOfAllIndexCatchupsInProgress() throws Exception {

        final CatchupInProgress eventCatchupInProgress = mock(CatchupInProgress.class);
        when(eventCatchupInProgress.getSubscriptionName()).thenReturn("different_catchup");

        catchupStateManager.addCatchupInProgress(eventCatchupInProgress, EVENT_CATCHUP);

        assertThat(catchupStateManager.getAllCatchupsInProgress(INDEX_CATCHUP).isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress("subscription_1", startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress("subscription_2", startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress("subscription_3", startedAt.plusMinutes(2));

        catchupStateManager.addCatchupInProgress(catchupInProgress_1, INDEX_CATCHUP);
        catchupStateManager.addCatchupInProgress(catchupInProgress_2, INDEX_CATCHUP);
        catchupStateManager.addCatchupInProgress(catchupInProgress_3, INDEX_CATCHUP);

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), INDEX_CATCHUP), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), INDEX_CATCHUP), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), INDEX_CATCHUP), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(INDEX_CATCHUP);

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupStateManager.removeCatchupInProgress(catchupInProgress_2.getSubscriptionName(), INDEX_CATCHUP);

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), INDEX_CATCHUP), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), INDEX_CATCHUP), is(false));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), INDEX_CATCHUP), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(INDEX_CATCHUP);

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupStateManager.clear(INDEX_CATCHUP);

        assertThat(catchupStateManager.noCatchupsInProgress(INDEX_CATCHUP), is(true));
        assertThat(catchupStateManager.getAllCatchupsInProgress(INDEX_CATCHUP).isEmpty(), is(true));
    }
}
