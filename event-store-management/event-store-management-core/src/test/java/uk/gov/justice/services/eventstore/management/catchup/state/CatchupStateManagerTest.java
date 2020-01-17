package uk.gov.justice.services.eventstore.management.catchup.state;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupFor;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand;

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
    public void shouldMaintainACacheOfAllEventCatchupsInProgress() throws Exception {

        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();

        final CatchupInProgress indexCatchupInProgress = mock(CatchupInProgress.class);
        when(indexCatchupInProgress.getCatchupFor()).thenReturn(mock(CatchupFor.class));

        catchupStateManager.addCatchupInProgress(indexCatchupInProgress, new IndexerCatchupCommand());

        assertThat(catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand).isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupFor catchupFor_1 = new CatchupFor("subscription_1", "component_1");
        final CatchupFor catchupFor_2 = new CatchupFor("subscription_2", "component_1");
        final CatchupFor catchupFor_3 = new CatchupFor("subscription_3", "component_2");

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress(catchupFor_1, startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress(catchupFor_2, startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress(catchupFor_3, startedAt.plusMinutes(2));

        catchupStateManager.addCatchupInProgress(catchupInProgress_1, eventCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_2, eventCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_3, eventCatchupCommand);

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getCatchupFor(), eventCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getCatchupFor(), eventCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getCatchupFor(), eventCatchupCommand), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand);

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupStateManager.removeCatchupInProgress(catchupInProgress_2.getCatchupFor(), eventCatchupCommand);

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getCatchupFor(), eventCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getCatchupFor(), eventCatchupCommand), is(false));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getCatchupFor(), eventCatchupCommand), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand);

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupStateManager.clear(eventCatchupCommand);

        assertThat(catchupStateManager.noCatchupsInProgress(eventCatchupCommand), is(true));
        assertThat(catchupStateManager.getAllCatchupsInProgress(eventCatchupCommand).isEmpty(), is(true));
    }

    @Test
    public void shouldMaintainACacheOfAllIndexCatchupsInProgress() throws Exception {

        final IndexerCatchupCommand indexerCatchupCommand = new IndexerCatchupCommand();

        final CatchupInProgress eventCatchupInProgress = mock(CatchupInProgress.class);
        when(eventCatchupInProgress.getCatchupFor()).thenReturn(mock(CatchupFor.class));

        catchupStateManager.addCatchupInProgress(eventCatchupInProgress, new EventCatchupCommand());

        assertThat(catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand).isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final CatchupFor catchupFor_1 = new CatchupFor("subscription_1", "component_1");
        final CatchupFor catchupFor_2 = new CatchupFor("subscription_2", "component_1");
        final CatchupFor catchupFor_3 = new CatchupFor("subscription_3", "component_2");

        final CatchupInProgress catchupInProgress_1 = new CatchupInProgress(catchupFor_1, startedAt);
        final CatchupInProgress catchupInProgress_2 = new CatchupInProgress(catchupFor_2, startedAt.plusMinutes(1));
        final CatchupInProgress catchupInProgress_3 = new CatchupInProgress(catchupFor_3, startedAt.plusMinutes(2));

        catchupStateManager.addCatchupInProgress(catchupInProgress_1, indexerCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_2, indexerCatchupCommand);
        catchupStateManager.addCatchupInProgress(catchupInProgress_3, indexerCatchupCommand);

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getCatchupFor(), indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getCatchupFor(), indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getCatchupFor(), indexerCatchupCommand), is(true));

        final List<CatchupInProgress> allCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand);

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, hasItems(catchupInProgress_1, catchupInProgress_2, catchupInProgress_3));

        final CatchupInProgress removedCatchupInProgress = catchupStateManager.removeCatchupInProgress(catchupInProgress_2.getCatchupFor(), indexerCatchupCommand);

        assertThat(removedCatchupInProgress, is(catchupInProgress_2));

        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_1.getCatchupFor(), indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_2.getCatchupFor(), indexerCatchupCommand), is(false));
        assertThat(catchupStateManager.isCatchupInProgress(catchupInProgress_3.getCatchupFor(), indexerCatchupCommand), is(true));

        final List<CatchupInProgress> currentCatchupsInProgress = catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand);

        assertThat(currentCatchupsInProgress.size(), is(2));
        assertThat(currentCatchupsInProgress, hasItems(catchupInProgress_1, catchupInProgress_3));

        catchupStateManager.clear(indexerCatchupCommand);

        assertThat(catchupStateManager.noCatchupsInProgress(indexerCatchupCommand), is(true));
        assertThat(catchupStateManager.getAllCatchupsInProgress(indexerCatchupCommand).isEmpty(), is(true));
    }
}
