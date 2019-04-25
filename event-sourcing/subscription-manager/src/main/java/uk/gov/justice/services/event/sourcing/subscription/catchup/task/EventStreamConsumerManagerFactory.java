package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventQueueConsumerFactory;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.ConsumeEventQueueBean;

import javax.inject.Inject;

public class EventStreamConsumerManagerFactory {

    @Inject
    private ConsumeEventQueueBean consumeEventQueueBean;

    @Inject
    private EventStreamsInProgressList eventStreamsInProgressList;

    @Inject
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    public EventStreamConsumerManager create() {
        return new ConcurrentEventStreamConsumerManager(
                eventStreamsInProgressList,
                consumeEventQueueBean,
                eventQueueConsumerFactory);
    }
}
