package uk.gov.justice.services.eventstore.management.indexer.process;

import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;

public class IndexerCatchupContext {

    private final String componentName;
    private final Subscription subscription;
    private final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent;

    public IndexerCatchupContext(final String componentName, final Subscription subscription,
                                 final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent) {
        this.componentName = componentName;
        this.subscription = subscription;
        this.indexerCatchupRequestedEvent = indexerCatchupRequestedEvent;
    }

    public String getComponentName() {
        return componentName;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public IndexerCatchupRequestedEvent getIndexerCatchupRequestedEvent() {
        return indexerCatchupRequestedEvent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexerCatchupContext)) return false;
        final IndexerCatchupContext that = (IndexerCatchupContext) o;
        return Objects.equals(componentName, that.componentName) &&
                Objects.equals(subscription, that.subscription) &&
                Objects.equals(indexerCatchupRequestedEvent, that.indexerCatchupRequestedEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentName, subscription, indexerCatchupRequestedEvent);
    }

    @Override
    public String toString() {
        return "IndexerCatchupContext{" +
                "componentName='" + componentName + '\'' +
                ", subscription=" + subscription +
                ", indexerCatchupRequestedEvent=" + indexerCatchupRequestedEvent +
                '}';
    }
}
