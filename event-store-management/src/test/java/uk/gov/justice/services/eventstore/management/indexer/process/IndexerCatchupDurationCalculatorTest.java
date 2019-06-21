package uk.gov.justice.services.eventstore.management.indexer.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexerCatchupDurationCalculatorTest {

    @InjectMocks
    private IndexerCatchupDurationCalculator indexerCatchupDurationCalculator;

    @Test
    public void shouldCalculateTheDurationOfIndexerCatchup() throws Exception {

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusSeconds(90);

        final String subscriptionName = "subscription";

        final IndexerCatchupInProgress indexerCatchupInProgress = new IndexerCatchupInProgress(
                subscriptionName,
                startedAt);

        final IndexerCatchupCompletedForSubscriptionEvent indexerCatchupCompletedForSubscriptionEvent = new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                23,
                completedAt);

        final Duration duration = indexerCatchupDurationCalculator.calculate(
                indexerCatchupInProgress,
                indexerCatchupCompletedForSubscriptionEvent);

        assertThat(duration.toMillis(), is(90_000L));
    }
}
