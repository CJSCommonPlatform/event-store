package uk.gov.justice.services.eventstore.management.catchup.process;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupInProgress {

    private final CatchupFor catchupFor;
    private final ZonedDateTime startedAt;

    public CatchupInProgress(final CatchupFor catchupFor, final ZonedDateTime startedAt) {
        this.catchupFor = catchupFor;
        this.startedAt = startedAt;
    }

    public CatchupFor getCatchupFor() {
        return catchupFor;
    }

    public ZonedDateTime getStartedAt() {
        return startedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupInProgress)) return false;
        final CatchupInProgress that = (CatchupInProgress) o;
        return Objects.equals(catchupFor, that.catchupFor) &&
                Objects.equals(startedAt, that.startedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchupFor, startedAt);
    }

    @Override
    public String toString() {
        return "CatchupInProgress{" +
                "catchupFor=" + catchupFor +
                ", startedAt=" + startedAt +
                '}';
    }
}
