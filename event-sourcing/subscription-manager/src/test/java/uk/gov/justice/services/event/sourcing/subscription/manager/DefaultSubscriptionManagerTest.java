package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
