package uk.gov.justice.services.event.sourcing.subscription.catchup.lifecycle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupCompletedForSubscriptionEvent;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupDurationCalculatorTest {

    @InjectMocks
    private CatchupDurationCalculator catchupDurationCalculator;

    @Test
    public void shouldCalculateTheDurationOfCatchup() throws Exception {

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusSeconds(90);

        final String subscriptionName = "subscription";

        final CatchupInProgress catchupInProgress = new CatchupInProgress(
                subscriptionName,
                startedAt);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                23,
                completedAt);

        final Duration duration = catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent);

        assertThat(duration.toMillis(), is(90_000L));
    }
}
