package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Objects;
import java.util.concurrent.Callable;

public class EventCatchupTask implements Callable<Boolean> {

    private final Subscription subscription;
    private final EventCatchupProcessorBean eventCatchupProcessorBean;

    public EventCatchupTask(
            final Subscription subscription,
            final EventCatchupProcessorBean eventCatchupProcessorBean) {
        this.subscription = subscription;
        this.eventCatchupProcessorBean = eventCatchupProcessorBean;
    }


    @Override
    public Boolean call() {
        eventCatchupProcessorBean.performEventCatchup(
                subscription
        );

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventCatchupTask)) return false;
        final EventCatchupTask that = (EventCatchupTask) o;
        return Objects.equals(subscription, that.subscription) &&
                Objects.equals(eventCatchupProcessorBean, that.eventCatchupProcessorBean);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription, eventCatchupProcessorBean);
    }
}
