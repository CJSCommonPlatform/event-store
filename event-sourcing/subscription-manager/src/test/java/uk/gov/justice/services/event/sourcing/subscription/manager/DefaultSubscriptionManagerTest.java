package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
