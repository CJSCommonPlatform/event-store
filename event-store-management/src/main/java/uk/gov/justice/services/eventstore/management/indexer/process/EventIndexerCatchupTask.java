package uk.gov.justice.services.eventstore.management.indexer.process;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;
import java.util.concurrent.Callable;

public class EventIndexerCatchupTask implements Callable<Boolean> {

    private final Subscription subscription;
    private final EventIndexerCatchupProcessorBean eventCatchupProcessorBean;
    private final String componentName;

    public EventIndexerCatchupTask(
            final Subscription subscription,
            final EventIndexerCatchupProcessorBean eventCatchupProcessorBean,
            final String componentName) {
        this.subscription = subscription;
        this.eventCatchupProcessorBean = eventCatchupProcessorBean;
        this.componentName = componentName;
    }


    @Override
    public Boolean call() {
        eventCatchupProcessorBean.performEventIndexerCatchup(
                subscription,
                componentName
        );

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventIndexerCatchupTask)) return false;
        final EventIndexerCatchupTask that = (EventIndexerCatchupTask) o;
        return Objects.equals(subscription, that.subscription) &&
                Objects.equals(eventCatchupProcessorBean, that.eventCatchupProcessorBean) &&
                Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription, eventCatchupProcessorBean, componentName);
    }
}
