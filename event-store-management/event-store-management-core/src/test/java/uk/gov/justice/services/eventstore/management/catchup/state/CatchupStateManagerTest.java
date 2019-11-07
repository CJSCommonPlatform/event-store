package uk.gov.justice.services.eventstore.management.catchup.state;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand;

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

        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();

        final CatchupInProgress indexCatchupInProgress = mock(CatchupInProgress.class);
        when(indexCatchupInProgress.getSubscriptionName()).thenReturn("different_catchup");

        catchupStateManager.addCatchupInProgress(indexCatchupInProgress, new IndexerCatchupCommand());

        assertThat(catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand).isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress("subscription_1", startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress("subscription_2", startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress("subscription_3", startedAt.plusMinutes(2));

        catchupStateManager.addCatchupInProgress(catchupInProgress_1, eventCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_2, eventCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_3, eventCatchupCommand);

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), eventCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), eventCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), eventCatchupCommand), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand);

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupStateManager.removeCatchupInProgress(catchupInProgress_2.getSubscriptionName(), eventCatchupCommand);

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), eventCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), eventCatchupCommand), is(false));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), eventCatchupCommand), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand);

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupStateManager.clear(eventCatchupCommand);

        assertThat(catchupStateManager.noCatchupsInProgress(eventCatchupCommand), is(true));
        assertThat(catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand).isEmpty(), is(true));
    }

    @Test
    public void shouldMaintainACacheOfAllIndexCatchupsInProgress() throws Exception {

        final IndexerCatchupCommand indexerCatchupCommand = new IndexerCatchupCommand();

        final CatchupInProgress eventCatchupInProgress = mock(CatchupInProgress.class);
        when(eventCatchupInProgress.getSubscriptionName()).thenReturn("different_catchup");

        catchupStateManager.addCatchupInProgress(eventCatchupInProgress, new EventCatchupCommand());

        assertThat(catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand).isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress("subscription_1", startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress("subscription_2", startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress("subscription_3", startedAt.plusMinutes(2));

        catchupStateManager.addCatchupInProgress(catchupInProgress_1, indexerCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_2, indexerCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_3, indexerCatchupCommand);

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), indexerCatchupCommand), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand);

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupStateManager.removeCatchupInProgress(catchupInProgress_2.getSubscriptionName(), indexerCatchupCommand);

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getSubscriptionName(), indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getSubscriptionName(), indexerCatchupCommand), is(false));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getSubscriptionName(), indexerCatchupCommand), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand);

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, CoreMatchers.hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupStateManager.clear(indexerCatchupCommand);

        assertThat(catchupStateManager.noCatchupsInProgress(indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand).isEmpty(), is(true));
    }
}
