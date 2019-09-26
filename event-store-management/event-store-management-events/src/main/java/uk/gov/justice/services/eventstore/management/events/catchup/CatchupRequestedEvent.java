package uk.gov.justice.services.eventstore.management.events.catchup;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupRequestedEvent {

    private final CatchupType catchupType;
    private final SystemCommand target;
    private final ZonedDateTime catchupRequestedAt;

    public CatchupRequestedEvent(
            final CatchupType catchupType,
            final SystemCommand target,
            final ZonedDateTime catchupRequestedAt) {
        this.catchupType = catchupType;
        this.target = target;
        this.catchupRequestedAt = catchupRequestedAt;
    }

    public CatchupType getCatchupType() {
        return catchupType;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getCatchupRequestedAt() {
        return catchupRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupRequestedEvent)) return false;
        final CatchupRequestedEvent that = (CatchupRequestedEvent) o;
        return catchupType == that.catchupType &&
                Objects.equals(target, that.target) &&
                Objects.equals(catchupRequestedAt, that.catchupRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchupType, target, catchupRequestedAt);
    }

    @Override
    public String toString() {
        return "CatchupRequestedEvent{" +
                "catchupType=" + catchupType +
                ", target=" + target +
                ", catchupRequestedAt=" + catchupRequestedAt +
                '}';
    }
}
