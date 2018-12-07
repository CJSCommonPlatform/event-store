package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventBufferProcessorTest {

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private EventBufferService eventBufferService;

    @Mock
    private InterceptorContextProvider interceptorContextProvider;

    @InjectMocks
    private EventBufferProcessor eventBufferProcessor;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldProcessAllEventsOnTheEventBuffer() throws Exception {


        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope otherEvent_1 = mock(JsonEnvelope.class);
        final JsonEnvelope otherEvent_2 = mock(JsonEnvelope.class);

        final InterceptorContext interceptorContext_1 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_2 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_3 = mock(InterceptorContext.class);

        final StreamWasClosedIndicator streamWasClosedIndicator = new StreamWasClosedIndicator();

        final Stream<JsonEnvelope> envelopeStream = Stream.of(incomingJsonEnvelope, otherEvent_1, otherEvent_2);

        envelopeStream.onClose(streamWasClosedIndicator::setClosed);

        assertThat(streamWasClosedIndicator.streamWasClosed(), is(false));

        when(eventBufferService.currentOrderedEventsWith(incomingJsonEnvelope)).thenReturn(envelopeStream);
        when(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope)).thenReturn(interceptorContext_1);
        when(interceptorContextProvider.getInterceptorContext(otherEvent_1)).thenReturn(interceptorContext_2);
        when(interceptorContextProvider.getInterceptorContext(otherEvent_2)).thenReturn(interceptorContext_3);

        eventBufferProcessor.processWithEventBuffer(incomingJsonEnvelope);

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
