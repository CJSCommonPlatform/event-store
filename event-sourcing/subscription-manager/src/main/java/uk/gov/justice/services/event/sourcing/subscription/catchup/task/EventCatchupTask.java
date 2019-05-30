package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;
import java.util.concurrent.Callable;

public class EventCatchupTask implements Callable<Boolean> {

    private final Subscription subscription;
    private final EventCatchupProcessorBean eventCatchupProcessorBean;
    private final String componentName;

    public EventCatchupTask(
            final Subscription subscription,
            final EventCatchupProcessorBean eventCatchupProcessorBean,
            final String componentName) {
        this.subscription = subscription;
        this.eventCatchupProcessorBean = eventCatchupProcessorBean;
        this.componentName = componentName;
    }


    @Override
    public Boolean call() {
        eventCatchupProcessorBean.performEventCatchup(
                subscription,
                componentName
        );

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventCatchupTask)) return false;
        final EventCatchupTask that = (EventCatchupTask) o;
        return Objects.equals(subscription, that.subscription) &&
                Objects.equals(eventCatchupProcessorBean, that.eventCatchupProcessorBean) &&
                Objects.equals(componentName, that.componentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription, eventCatchupProcessorBean, componentName);
    }
}
