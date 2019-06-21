package uk.gov.justice.services.eventstore.management.indexer.process;

import static java.time.Duration.between;

import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;

import java.time.Duration;
import java.time.Instant;

public class IndexerCatchupDurationCalculator {

    public Duration calculate(
            final IndexerCatchupInProgress catchupInProgress,
            final IndexerCatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        final Instant startedAt = catchupInProgress.getStartedAt().toInstant();
        final Instant completedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt().toInstant();
        
        return between(startedAt, completedAt);
    }
}

