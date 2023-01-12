package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupDurationCalculatorTest {

    @InjectMocks
    private CatchupDurationCalculator catchupDurationCalculator;

    @Test
    public void shouldCalculateTheDurationOfCatchup() throws Exception {

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusSeconds(90);

        final Duration duration = catchupDurationCalculator.calculate(
                startedAt,
                completedAt);

        assertThat(duration.toMillis(), is(90_000L));
    }
}
