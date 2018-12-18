package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventBufferProcessorFactoryTest {



    @InjectMocks
    private EventBufferProcessorFactory eventBufferProcessorFactory;

    @Test
    public void shouldCreateEventBufferProcessor() throws Exception {

        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);
        final EventBufferService eventBufferService = mock(EventBufferService.class);
        final InterceptorContextProvider interceptorContextProvider = mock(InterceptorContextProvider.class);

        final EventBufferProcessor eventBufferProcessor = eventBufferProcessorFactory.create(interceptorChainProcessor, eventBufferService, interceptorContextProvider);

        assertThat(getValueOfField(eventBufferProcessor, "interceptorChainProcessor", InterceptorChainProcessor.class), is(interceptorChainProcessor));
        assertThat(getValueOfField(eventBufferProcessor, "eventBufferService", EventBufferService.class), is(eventBufferService));
        assertThat(getValueOfField(eventBufferProcessor, "interceptorContextProvider", InterceptorContextProvider.class), is(interceptorContextProvider));
    }
}
