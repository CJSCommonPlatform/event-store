package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.jmx.command.SystemCommand;

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
        final SystemCommand systemCommand = mock(SystemCommand.class);

        final String subscriptionName = "subscription";

        final CatchupInProgress catchupInProgress = new CatchupInProgress(
                subscriptionName,
                startedAt);

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                23,
                systemCommand,
                completedAt);

        final Duration duration = catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent);

        assertThat(duration.toMillis(), is(90_000L));
    }
}
