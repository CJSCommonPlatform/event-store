package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

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

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupProcessorFactoryTest {

    @Mock
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Mock
    private EventBufferService eventBufferService;

    @Mock
    private ProcessedEventTrackingService processedEventTrackingService;

    @Mock
    private EventSourceProvider eventSourceProvider;

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @Mock
    private ManagedExecutorService managedExecutorService;

    @Mock
    private EventStreamsInProgressList eventStreamsInProgressList;

    @Mock
    private ConcurrentEventStreamConsumerManager eventStreamConsumerManager;

    @Mock
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

    @Mock
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Mock
    UtcClock clock;

    @InjectMocks
    private EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Test
    public void shouldCreateEventCatchupProcessorFactory() throws Exception {

        final String componentName = "component name";

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);

        when(interceptorChainProcessorProducer.produceLocalProcessor(componentName)).thenReturn(interceptorChainProcessor);

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.createFor(componentName);

        assertThat(getValueOfField(eventCatchupProcessor, "processedEventTrackingService", ProcessedEventTrackingService.class), is(processedEventTrackingService));
        assertThat(getValueOfField(eventCatchupProcessor, "eventSourceProvider", EventSourceProvider.class), is(eventSourceProvider));
        assertThat(getValueOfField(eventCatchupProcessor, "eventStreamConsumerManager", EventStreamConsumerManager.class), is(instanceOf(ConcurrentEventStreamConsumerManager.class)));
        assertThat(getValueOfField(eventCatchupProcessor, "catchupStartedForSubscriptionEventFirer", Event.class), is(catchupStartedForSubscriptionEventFirer));
        assertThat(getValueOfField(eventCatchupProcessor, "catchupCompletedForSubscriptionEventFirer", Event.class), is(catchupCompletedForSubscriptionEventFirer));
        assertThat(getValueOfField(eventCatchupProcessor, "clock", UtcClock.class), is(clock));


        final EventStreamConsumerManager eventStreamConsumerManager = getValueOfField(eventCatchupProcessor, "eventStreamConsumerManager", EventStreamConsumerManager.class);
        final ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory = getValueOfField(eventStreamConsumerManager, "consumeEventQueueTaskFactory", ConsumeEventQueueTaskFactory.class);
        final TransactionalEventProcessor transactionalEventProcessor = getValueOfField(consumeEventQueueTaskFactory, "transactionalEventProcessor", TransactionalEventProcessor.class);
        final EventBufferProcessor eventBufferProcessor = getValueOfField(transactionalEventProcessor, "eventBufferProcessor", EventBufferProcessor.class);

        assertThat(getValueOfField(eventBufferProcessor, "interceptorChainProcessor", InterceptorChainProcessor.class), is(interceptorChainProcessor));
        assertThat(getValueOfField(eventBufferProcessor, "eventBufferService", EventBufferService.class), is(eventBufferService));
        assertThat(getValueOfField(eventBufferProcessor, "interceptorContextProvider", InterceptorContextProvider.class), is(interceptorContextProvider));
    }
}
