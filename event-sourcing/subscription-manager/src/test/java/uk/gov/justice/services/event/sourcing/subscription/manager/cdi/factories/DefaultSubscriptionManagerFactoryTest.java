package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.DefaultSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultSubscriptionManagerFactoryTest {

    @Mock
    InterceptorContextProvider interceptorContextProvider;

    @Mock
    EventBufferService eventBufferService;

    @Mock
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @InjectMocks
    private DefaultSubscriptionManagerFactory defaultSubscriptionManagerFactory;

    @Test
    public void shouldCreateDefaultSubscriptionManagerFactory() throws Exception {

        final String componentName = "componentName";

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);

        when(interceptorChainProcessorProducer.produceLocalProcessor(componentName)).thenReturn(interceptorChainProcessor);

        final DefaultSubscriptionManager defaultSubscriptionManager = (DefaultSubscriptionManager) defaultSubscriptionManagerFactory.create(componentName);

        final EventBufferProcessor eventBufferProcessor = getValueOfField(defaultSubscriptionManager, "eventBufferProcessor", EventBufferProcessor.class);

        assertThat(eventBufferProcessor, is(notNullValue()));

        assertThat(getValueOfField(eventBufferProcessor, "interceptorChainProcessor", InterceptorChainProcessor.class), is(interceptorChainProcessor));
        assertThat(getValueOfField(eventBufferProcessor, "eventBufferService", EventBufferService.class), is(eventBufferService));
        assertThat(getValueOfField(eventBufferProcessor, "interceptorContextProvider", InterceptorContextProvider.class), is(interceptorContextProvider));
        assertThat(getValueOfField(eventBufferProcessor, "component", String.class), is(componentName));
    }
}
