package uk.gov.justice.services.eventstore.management.replay.process;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionReplayEventProcessorTest {

    @Mock
    private EventBufferProcessorFactory eventBufferProcessorFactory;

    @InjectMocks
    private TransactionReplayEventProcessor transactionReplayEventProcessor;

    @Test
    void shouldCreateEventBufferProcessorAndInvokeProcess() {
        final String componentName = "componentName";
        final JsonEnvelope eventEnvelope = mock(JsonEnvelope.class);
        final EventBufferProcessor eventBufferProcessor = mock(EventBufferProcessor.class);
        when(eventBufferProcessorFactory.create(componentName)).thenReturn(eventBufferProcessor);

        transactionReplayEventProcessor.processWithEventBuffer(componentName, eventEnvelope);

        verify(eventBufferProcessor).processWithEventBuffer(eventEnvelope);
    }
}