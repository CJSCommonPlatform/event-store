package uk.gov.justice.services.eventstore.management.indexer.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class IndexerCatchupCompletedForSubscriptionEvent {

    private final String subscriptionName;
    private final String eventSourceName;
    private final String componentName;
    private final SystemCommand target;
    private final ZonedDateTime indexerCatchupCompletedAt;
    private final int totalNumberOfEvents;

    public IndexerCatchupCompletedForSubscriptionEvent(
            final String subscriptionName,
            final String eventSourceName,
            final String componentName,
            final SystemCommand target,
            final ZonedDateTime indexerCatchupCompletedAt,
            final int totalNumberOfEvents) {
        this.subscriptionName = subscriptionName;
        this.eventSourceName = eventSourceName;
        this.componentName = componentName;
        this.target = target;
        this.indexerCatchupCompletedAt = indexerCatchupCompletedAt;
        this.totalNumberOfEvents = totalNumberOfEvents;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    public String getComponentName() {
        return componentName;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getIndexerCatchupCompletedAt() {
        return indexerCatchupCompletedAt;
    }

    public int getTotalNumberOfEvents() {
        return totalNumberOfEvents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupCompletedForSubscriptionEvent)) return false;
        final IndexerCatchupCompletedForSubscriptionEvent that = (IndexerCatchupCompletedForSubscriptionEvent) o;
        return totalNumberOfEvents == that.totalNumberOfEvents &&
                Objects.equals(subscriptionName, that.subscriptionName) &&
                Objects.equals(eventSourceName, that.eventSourceName) &&
                Objects.equals(componentName, that.componentName) &&
                Objects.equals(target, that.target) &&
                Objects.equals(indexerCatchupCompletedAt, that.indexerCatchupCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName, eventSourceName, componentName, target, indexerCatchupCompletedAt, totalNumberOfEvents);
    }

    @Override
    public String toString() {
        return "IndexerCatchupCompletedForSubscriptionEvent{" +
                "subscriptionName='" + subscriptionName + '\'' +
                ", eventSourceName='" + eventSourceName + '\'' +
                ", componentName='" + componentName + '\'' +
                ", target=" + target +
                ", indexerCatchupCompletedAt=" + indexerCatchupCompletedAt +
                ", totalNumberOfEvents=" + totalNumberOfEvents +
                '}';
    }
}
