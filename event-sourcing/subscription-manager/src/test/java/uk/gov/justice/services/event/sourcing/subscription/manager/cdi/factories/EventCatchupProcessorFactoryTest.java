package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.event.sourcing.subscription.startup.EventCatchupProcessor;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueTaskFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupProcessorFactoryTest {

    @Mock
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Mock
    private EventBufferService eventBufferService;

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private EventSourceProvider eventSourceProvider;

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @InjectMocks
    private EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Test
    public void shouldCreateEventCatchupProcessorFactory() throws Exception {

        final String componentName = "component name";

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);

        when(interceptorChainProcessorProducer.produceLocalProcessor(componentName)).thenReturn(interceptorChainProcessor);

        final EventCatchupProcessor eventCatchupProcessor = eventCatchupProcessorFactory.createFor(componentName);

        assertThat(getValueOfField(eventCatchupProcessor, "subscriptionsRepository", SubscriptionsRepository.class), is(subscriptionsRepository));
        assertThat(getValueOfField(eventCatchupProcessor, "logger", Logger.class), is(notNullValue()));

        final EventStreamConsumerManager eventStreamConsumerManager = getValueOfField(eventCatchupProcessor, "eventStreamConsumerManager", EventStreamConsumerManager.class);
        final ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory = getValueOfField(eventStreamConsumerManager, "consumeEventQueueTaskFactory", ConsumeEventQueueTaskFactory.class);
        final TransactionalEventProcessor transactionalEventProcessor = getValueOfField(consumeEventQueueTaskFactory, "transactionalEventProcessor", TransactionalEventProcessor.class);
        final EventBufferProcessor eventBufferProcessor = getValueOfField(transactionalEventProcessor, "eventBufferProcessor", EventBufferProcessor.class);

        assertThat(getValueOfField(eventBufferProcessor, "interceptorChainProcessor", InterceptorChainProcessor.class), is(interceptorChainProcessor));
        assertThat(getValueOfField(eventBufferProcessor, "eventBufferService", EventBufferService.class), is(eventBufferService));
        assertThat(getValueOfField(eventBufferProcessor, "interceptorContextProvider", InterceptorContextProvider.class), is(interceptorContextProvider));
    }
}
