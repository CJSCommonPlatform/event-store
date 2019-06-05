package uk.gov.justice.services.eventstore.management.rebuild.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RebuildRequestedEvent {

    private final SystemCommand cause;
    private final ZonedDateTime rebuildRequestedAt;

    public RebuildRequestedEvent(final ZonedDateTime rebuildRequestedAt, final SystemCommand cause) {
        this.cause = cause;
        this.rebuildRequestedAt = rebuildRequestedAt;
    }

    public SystemCommand getCause() {
        return cause;
    }

    public ZonedDateTime getRebuildRequestedAt() {
        return rebuildRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RebuildRequestedEvent)) return false;
        final RebuildRequestedEvent that = (RebuildRequestedEvent) o;
        return Objects.equals(cause, that.cause) &&
                Objects.equals(rebuildRequestedAt, that.rebuildRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cause, rebuildRequestedAt);
    }

    @Override
    public String toString() {
        return "RebuildRequestedEvent{" +
                "cause=" + cause +
                ", rebuildRequestedAt=" + rebuildRequestedAt +
                '}';
    }
}
