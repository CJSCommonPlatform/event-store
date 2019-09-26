package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.time.Duration.between;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;

import java.time.Duration;
import java.time.Instant;

public class CatchupDurationCalculator {

    public Duration calculate(
            final CatchupInProgress catchupInProgress,
            final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        final Instant startedAt = catchupInProgress.getStartedAt().toInstant();
        final Instant completedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt().toInstant();
        
        return between(startedAt, completedAt);
    }
}

