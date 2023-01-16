package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupEventBufferProcessorTest {

    private static final String SUBSCRIPTION_NAME = "subscription_name";
    private static final String EVENT_LISTENER = "event_listener";

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private EventBufferService eventBufferService;

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Mock
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Captor
    private ArgumentCaptor<InterceptorContext> interceptorContextCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> jsonEnvelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @InjectMocks
    private CatchupEventBufferProcessor catchupEventBufferProcessor;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProcessEnvelopeWithEventBuffer(){
        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = mock(InterceptorContext.class);


        final Stream<JsonEnvelope> events = Stream.of(incomingJsonEnvelope);

        when(subscriptionsDescriptorsRegistry.findComponentNameBy(SUBSCRIPTION_NAME))
                .thenReturn(EVENT_LISTENER);

        when(interceptorChainProcessorProducer.produceLocalProcessor(EVENT_LISTENER))
                .thenReturn(interceptorChainProcessor);

        when(eventBufferService.currentOrderedEventsWith(incomingJsonEnvelope, EVENT_LISTENER))
                .thenReturn(events);

        when(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope))
                .thenReturn(interceptorContext);

        catchupEventBufferProcessor.processWithEventBuffer(incomingJsonEnvelope, SUBSCRIPTION_NAME);

        verify(eventBufferService).currentOrderedEventsWith(jsonEnvelopeArgumentCaptor.capture(), stringArgumentCaptor.capture());
        verify(interceptorContextProvider).getInterceptorContext(jsonEnvelopeArgumentCaptor.capture());
        verify(interceptorChainProcessor).process(interceptorContextCaptor.capture());

        assertThat(interceptorContextCaptor.getValue(), is(interceptorContext));
    }
}