package uk.gov.justice.services.eventstore.management.catchup.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupRequestedEvent {

    private final SystemCommand cause;
    private final ZonedDateTime catchupRequestedAt;

    public CatchupRequestedEvent(final SystemCommand cause, final ZonedDateTime catchupRequestedAt) {
        this.cause = cause;
        this.catchupRequestedAt = catchupRequestedAt;
    }

    public SystemCommand getCause() {
        return cause;
    }

    public ZonedDateTime getCatchupRequestedAt() {
        return catchupRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupRequestedEvent)) return false;
        final CatchupRequestedEvent that = (CatchupRequestedEvent) o;
        return Objects.equals(cause, that.cause) &&
                Objects.equals(catchupRequestedAt, that.catchupRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cause, catchupRequestedAt);
    }

    @Override
    public String toString() {
        return "CatchupRequestedEvent{" +
                "cause=" + cause +
                ", catchupRequestedAt=" + catchupRequestedAt +
                '}';
    }
}
