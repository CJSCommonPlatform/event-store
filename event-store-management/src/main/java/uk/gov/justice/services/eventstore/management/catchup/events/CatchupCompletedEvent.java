package uk.gov.justice.services.eventstore.management.catchup.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupCompletedEvent {

    private final ZonedDateTime completedAt;

    public CatchupCompletedEvent(final ZonedDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupCompletedEvent)) return false;
        final CatchupCompletedEvent that = (CatchupCompletedEvent) o;
        return Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(completedAt);
    }

    @Override
    public String toString() {
        return "CatchupCompletedEvent{" +
                "completedAt=" + completedAt +
                '}';
    }
}
