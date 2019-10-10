package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.time.Duration.between;

import java.time.Duration;
import java.time.ZonedDateTime;

public class CatchupDurationCalculator {

    public Duration calculate(final ZonedDateTime catchupStartedAt, final ZonedDateTime catchupCompletedAt) {
        return between(catchupStartedAt.toInstant(), catchupCompletedAt.toInstant());
    }
}

