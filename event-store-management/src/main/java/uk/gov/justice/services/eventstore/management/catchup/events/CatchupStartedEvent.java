package uk.gov.justice.services.eventstore.management.catchup.events;

import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupStartedEvent {

    private final CatchupType catchupType;
    private final ZonedDateTime catchupStartedAt;

    public CatchupStartedEvent(final CatchupType catchupType, final ZonedDateTime catchupStartedAt) {
        this.catchupType = catchupType;
        this.catchupStartedAt = catchupStartedAt;
    }

    public CatchupType getCatchupType() {
        return catchupType;
    }

    public ZonedDateTime getCatchupStartedAt() {
        return catchupStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupStartedEvent)) return false;
        final CatchupStartedEvent that = (CatchupStartedEvent) o;
        return catchupType == that.catchupType &&
                Objects.equals(catchupStartedAt, that.catchupStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchupType, catchupStartedAt);
    }

    @Override
    public String toString() {
        return "CatchupStartedEvent{" +
                "catchupType=" + catchupType +
                ", catchupStartedAt=" + catchupStartedAt +
                '}';
    }
}
