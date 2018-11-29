package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSubscriptionManagerTest {


    @Mock
    private EventBufferProcessor eventBufferProcessor;

    @Mock
    private EventCatchupProcessor eventCatchupProcessor;

    @InjectMocks
    private DefaultSubscriptionManager defaultSubscriptionManager;

    @Test
    public void shouldProcessWithEventBuffer() {


        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);

        defaultSubscriptionManager.process(incomingJsonEnvelope);

        verify(eventBufferProcessor).processWithEventBuffer(incomingJsonEnvelope);
    }

    @Test
    public void shouldPerformEventCatchupOnStartupIfTheEventBufferIsPresent() {

        defaultSubscriptionManager.startSubscription();

        verify(eventCatchupProcessor).performEventCatchup();
    }
}
