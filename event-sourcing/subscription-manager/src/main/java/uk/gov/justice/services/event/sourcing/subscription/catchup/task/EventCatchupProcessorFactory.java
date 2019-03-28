package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueTaskFactory;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventCatchupProcessorFactory {

    @Inject
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    private EventBufferService eventBufferService;

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private EventSourceProvider eventSourceProvider;

    @Inject
    private InterceptorContextProvider interceptorContextProvider;

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private EventStreamsInProgressList eventStreamsInProgressList;

    @Inject
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

    @Inject
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Inject
    private UtcClock clock;

    public EventCatchupProcessor createFor(final String componentName) {

        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer
                .produceLocalProcessor(componentName);

        final EventBufferProcessor eventBufferProcessor = new EventBufferProcessor(
                interceptorChainProcessor,
                eventBufferService,
                interceptorContextProvider);

        final TransactionalEventProcessor transactionalEventProcessor = new TransactionalEventProcessor(eventBufferProcessor);
        final ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory = new ConsumeEventQueueTaskFactory(transactionalEventProcessor);
        final EventStreamConsumerManager eventStreamConsumerManager = new ConcurrentEventStreamConsumerManager(
                managedExecutorService,
                consumeEventQueueTaskFactory,
                eventStreamsInProgressList);

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
