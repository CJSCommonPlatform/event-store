package uk.gov.justice.services.eventstore.management.indexer.process;

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
public class IndexerCatchupsInProgressCacheTest {

    @InjectMocks
    private IndexerCatchupsInProgressCache indexerCatchupsInProgressCache;

    @Test
    public void shouldMaintainACacheOfAllCatchupsInProgress() throws Exception {

        assertThat(indexerCatchupsInProgressCache.getAllCatchupsInProgress().isEmpty(), is(true));

        final ZonedDateTime startedAt = new UtcClock().now();

        final IndexerCatchupInProgress indexerCatchupInProgress_1 = new IndexerCatchupInProgress("subscription_1", startedAt);
        final IndexerCatchupInProgress indexerCatchupInProgress_2 = new IndexerCatchupInProgress("subscription_2", startedAt.plusMinutes(1));
        final IndexerCatchupInProgress indexerCatchupInProgress_3 = new IndexerCatchupInProgress("subscription_3", startedAt.plusMinutes(2));

        indexerCatchupsInProgressCache.addCatchupInProgress(indexerCatchupInProgress_1);
        indexerCatchupsInProgressCache.addCatchupInProgress(indexerCatchupInProgress_2);
        indexerCatchupsInProgressCache.addCatchupInProgress(indexerCatchupInProgress_3);

        assertThat(indexerCatchupsInProgressCache.isCatchupInProgress(indexerCatchupInProgress_1.getSubscriptionName()), is(true));
        assertThat(indexerCatchupsInProgressCache.isCatchupInProgress(indexerCatchupInProgress_2.getSubscriptionName()), is(true));
        assertThat(indexerCatchupsInProgressCache.isCatchupInProgress(indexerCatchupInProgress_3.getSubscriptionName()), is(true));

        final List<IndexerCatchupInProgress> allCatchupsInProgress = indexerCatchupsInProgressCache.getAllCatchupsInProgress();

        assertThat(allCatchupsInProgress.size(), is(3));
        assertThat(allCatchupsInProgress, CoreMatchers.hasItems(indexerCatchupInProgress_1, indexerCatchupInProgress_2, indexerCatchupInProgress_3));

        final IndexerCatchupInProgress removedCatchupInProgress = indexerCatchupsInProgressCache.removeCatchupInProgress(indexerCatchupInProgress_2.getSubscriptionName());

        assertThat(removedCatchupInProgress, is(indexerCatchupInProgress_2));

        assertThat(indexerCatchupsInProgressCache.isCatchupInProgress(indexerCatchupInProgress_1.getSubscriptionName()), is(true));
        assertThat(indexerCatchupsInProgressCache.isCatchupInProgress(indexerCatchupInProgress_2.getSubscriptionName()), is(false));
        assertThat(indexerCatchupsInProgressCache.isCatchupInProgress(indexerCatchupInProgress_3.getSubscriptionName()), is(true));

        final List<IndexerCatchupInProgress> currentIndexerCatchupsInProgress = indexerCatchupsInProgressCache.getAllCatchupsInProgress();

        assertThat(currentIndexerCatchupsInProgress.size(), is(2));
        assertThat(currentIndexerCatchupsInProgress, CoreMatchers.hasItems(indexerCatchupInProgress_1, indexerCatchupInProgress_3));

        indexerCatchupsInProgressCache.removeAll();

        assertThat(indexerCatchupsInProgressCache.noCatchupsInProgress(), is(true));
        assertThat(indexerCatchupsInProgressCache.getAllCatchupsInProgress().isEmpty(), is(true));
    }
}
