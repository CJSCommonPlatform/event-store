package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamErrorRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class EventBufferProcessorTest {

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private EventBufferService eventBufferService;

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @Mock
    private StreamErrorRepository streamErrorRepository;

    @InjectMocks
    private EventBufferProcessor eventBufferProcessor;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldProcessAllEventsOnTheEventBuffer() throws Exception {

        final JsonEnvelope eventEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope eventEnvelope_2 = mock(JsonEnvelope.class);
        final JsonEnvelope eventEnvelope_3 = mock(JsonEnvelope.class);

        final InterceptorContext interceptorContext_1 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_2 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_3 = mock(InterceptorContext.class);

        final StreamWasClosedIndicator streamWasClosedIndicator = new StreamWasClosedIndicator();

        final Stream<JsonEnvelope> envelopeStream = Stream.of(eventEnvelope_1, eventEnvelope_2, eventEnvelope_3);

        envelopeStream.onClose(streamWasClosedIndicator::setClosed);

        assertThat(streamWasClosedIndicator.streamWasClosed(), is(false));

        when(eventBufferService.currentOrderedEventsWith(eventEnvelope_1, interceptorContext_1.getComponentName())).thenReturn(envelopeStream);
        when(interceptorContextProvider.getInterceptorContext(eventEnvelope_1)).thenReturn(interceptorContext_1);
        when(interceptorContextProvider.getInterceptorContext(eventEnvelope_2)).thenReturn(interceptorContext_2);
        when(interceptorContextProvider.getInterceptorContext(eventEnvelope_3)).thenReturn(interceptorContext_3);

        eventBufferProcessor.processWithEventBuffer(eventEnvelope_1);

        verify(interceptorChainProcessor).process(interceptorContext_1);
        verify(interceptorChainProcessor).process(interceptorContext_2);
        verify(interceptorChainProcessor).process(interceptorContext_3);

        assertThat(streamWasClosedIndicator.streamWasClosed(), is(true));
    }

    private class StreamWasClosedIndicator {
        private boolean closed = false;

        boolean streamWasClosed() {
            return closed;
        }

        void setClosed() {
            closed = true;
        }
    }
}
