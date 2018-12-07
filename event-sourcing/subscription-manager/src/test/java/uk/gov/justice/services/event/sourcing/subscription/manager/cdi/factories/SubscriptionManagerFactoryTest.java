package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.BackwardsCompatibleSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.DefaultSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferSelector;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionManagerFactoryTest {

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry;

    @Mock
    private EventSourceProvider eventSourceProvider;

    @Mock
    private EventBufferProcessorFactory eventBufferProcessorFactory;

    @Mock
    private EventBufferSelector eventBufferSelector;

    @Mock
    private EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Mock
    private EventCatchupProcessor eventCatchupProcessor;

    @Mock
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @InjectMocks
    private SubscriptionManagerFactory subscriptionManagerFactory;

    @Test
    public void shouldCreateDefaultSubscriptionManagerIfTheEventBufferServiceExists() throws Exception {

        final String subscriptionName = "subscriptionName";
        final String eventSourceName = "eventSourceName";
        final String componentName = "componentName";

        final EventSource eventSource = mock(EventSource.class);
        final Subscription subscription = mock(Subscription.class);

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);
        final EventBufferProcessor eventBufferProcessor = mock(EventBufferProcessor.class);
        final EventBufferService eventBufferService = mock(EventBufferService.class);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName)).thenReturn(componentName);
        when(eventSourceProvider.getEventSource(eventSourceName)).thenReturn(eventSource);
        when(interceptorChainProcessorProducer.produceLocalProcessor(componentName)).thenReturn(interceptorChainProcessor);
        when(eventBufferSelector.selectFor(componentName)).thenReturn(of(eventBufferService));
        when(eventBufferProcessorFactory.create(interceptorChainProcessor, eventBufferService, interceptorContextProvider)).thenReturn(eventBufferProcessor);
        when(eventCatchupProcessorFactory.create(subscription, eventSource, eventBufferProcessor)).thenReturn(eventCatchupProcessor);

        final DefaultSubscriptionManager defaultSubscriptionManager = (DefaultSubscriptionManager) subscriptionManagerFactory.create(subscription);

        assertThat(getValueOfField(defaultSubscriptionManager, "eventBufferProcessor", EventBufferProcessor.class), is(eventBufferProcessor));
        assertThat(getValueOfField(defaultSubscriptionManager, "eventCatchupProcessor", EventCatchupProcessor.class), is(eventCatchupProcessor));
    }

    @Test
    public void shouldCreateBackwardsCompatibleSubscriptionManagerIfNoEventBufferServiceExists() throws Exception {

        final String subscriptionName = "subscriptionName";
        final String eventSourceName = "eventSourceName";
        final String componentName = "componentName";

        final EventSource eventSource = mock(EventSource.class);
        final Subscription subscription = mock(Subscription.class);

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName)).thenReturn(componentName);
        when(eventSourceProvider.getEventSource(eventSourceName)).thenReturn(eventSource);
        when(interceptorChainProcessorProducer.produceLocalProcessor(componentName)).thenReturn(interceptorChainProcessor);
        when(eventBufferSelector.selectFor(componentName)).thenReturn(empty());

        final BackwardsCompatibleSubscriptionManager backwardsCompatibleSubscriptionManager = (BackwardsCompatibleSubscriptionManager) subscriptionManagerFactory.create(subscription);

        assertThat(getValueOfField(backwardsCompatibleSubscriptionManager, "interceptorChainProcessor", InterceptorChainProcessor.class), is(interceptorChainProcessor));
    }
}
