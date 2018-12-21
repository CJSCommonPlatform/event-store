package uk.gov.justice.services.event.sourcing.subscription.startup;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;

public class EventCatchupTask implements Runnable {

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
    public void run() {
        eventCatchupProcessorBean.performEventCatchup(
                componentName,
                subscription
        );
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
