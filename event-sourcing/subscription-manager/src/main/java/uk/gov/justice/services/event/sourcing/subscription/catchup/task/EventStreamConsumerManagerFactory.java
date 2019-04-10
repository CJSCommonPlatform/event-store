package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.services.event.sourcing.subscription.startup.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventQueueConsumerFactory;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueBean;

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
