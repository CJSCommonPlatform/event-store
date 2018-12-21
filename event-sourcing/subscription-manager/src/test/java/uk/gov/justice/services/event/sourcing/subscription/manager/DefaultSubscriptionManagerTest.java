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

    @InjectMocks
    private DefaultSubscriptionManager defaultSubscriptionManager;

    @Test
    public void shouldProcessWithEventBuffer() {

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);

        defaultSubscriptionManager.process(incomingJsonEnvelope);

        verify(eventBufferProcessor).processWithEventBuffer(incomingJsonEnvelope);
    }
}
