package uk.gov.justice.services.event.sourcing.subscription.catchup.lifecycle;

import static java.time.Duration.between;

import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupCompletedForSubscriptionEvent;

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

