package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import uk.gov.justice.services.event.sourcing.subscription.startup.EventCatchupProcessorBean;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;
import java.util.concurrent.Callable;

public class EventCatchupTask implements Callable<Boolean> {

    private final String componentName;
    private final Subscription subscription;
    private final EventCatchupProcessorBean eventCatchupProcessorBean;

    public EventCatchupTask(
            final String componentName,
            final Subscription subscription,
            final EventCatchupProcessorBean eventCatchupProcessorBean) {
        this.componentName = componentName;
        this.subscription = subscription;
        this.eventCatchupProcessorBean = eventCatchupProcessorBean;
    }


    @Override
    public Boolean call() {
        eventCatchupProcessorBean.performEventCatchup(
                componentName,
                subscription
        );

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EventCatchupTask that = (EventCatchupTask) o;
        return Objects.equals(componentName, that.componentName) &&
                Objects.equals(subscription, that.subscription) &&
                Objects.equals(eventCatchupProcessorBean, that.eventCatchupProcessorBean);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentName, subscription, eventCatchupProcessorBean);
    }
}
