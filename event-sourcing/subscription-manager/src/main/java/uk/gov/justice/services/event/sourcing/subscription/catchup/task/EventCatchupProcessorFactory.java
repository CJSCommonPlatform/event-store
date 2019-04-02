package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventQueueConsumerFactory;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueBean;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventCatchupProcessorFactory {

    @Inject
    private ConsumeEventQueueBean consumeEventQueueBean;

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private EventSourceProvider eventSourceProvider;

    @Inject
    private TransactionalEventProcessor transactionalEventProcessor;

    @Inject
    private EventStreamsInProgressList eventStreamsInProgressList;

    @Inject
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    @Inject
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

    @Inject
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Inject
    private UtcClock clock;

    public EventCatchupProcessor createFor() {

        final EventStreamConsumerManager eventStreamConsumerManager = new ConcurrentEventStreamConsumerManager(
                eventStreamsInProgressList,
                consumeEventQueueBean,
                transactionalEventProcessor,
                eventQueueConsumerFactory);

        return new EventCatchupProcessor(
                processedEventTrackingService,
                eventSourceProvider,
                eventStreamConsumerManager,
                catchupStartedForSubscriptionEventFirer,
                catchupCompletedForSubscriptionEventFirer,
                clock
        );
    }
}
