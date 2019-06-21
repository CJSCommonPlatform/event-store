package uk.gov.justice.services.eventstore.management.catchup.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CatchupCompletedForSubscriptionEvent {

    private final String eventSourceName;
    private final SystemCommand target;
    private final ZonedDateTime catchupCompletedAt;
    private final int totalNumberOfEvents;

    public CatchupCompletedForSubscriptionEvent(
            final String eventSourceName,
            final int totalNumberOfEvents,
            final SystemCommand target,
            final ZonedDateTime catchupCompletedAt) {

        this.eventSourceName = eventSourceName;
        this.target = target;
        this.catchupCompletedAt = catchupCompletedAt;
        this.totalNumberOfEvents = totalNumberOfEvents;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getCatchupCompletedAt() {
        return catchupCompletedAt;
    }

    public int getTotalNumberOfEvents() {
        return totalNumberOfEvents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchupCompletedForSubscriptionEvent)) return false;
        final CatchupCompletedForSubscriptionEvent that = (CatchupCompletedForSubscriptionEvent) o;
        return totalNumberOfEvents == that.totalNumberOfEvents &&
                Objects.equals(eventSourceName, that.eventSourceName) &&
                Objects.equals(target, that.target) &&
                Objects.equals(catchupCompletedAt, that.catchupCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventSourceName, target, catchupCompletedAt, totalNumberOfEvents);
    }

    @Override
    public String toString() {
        return "CatchupCompletedForSubscriptionEvent{" +
                "eventSourceName='" + eventSourceName + '\'' +
                ", target=" + target +
                ", catchupCompletedAt=" + catchupCompletedAt +
                ", totalNumberOfEvents=" + totalNumberOfEvents +
                '}';
    }
}
