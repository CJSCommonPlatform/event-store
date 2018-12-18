package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;

public class TransactionalEventProcessorTest {

    private final EventBufferProcessor eventBufferProcessor = mock(EventBufferProcessor.class);

    private final TransactionalEventProcessor transactionalEventProcessor = new TransactionalEventProcessor(eventBufferProcessor);

    @Test
    public void shouldProcessWithEventBufferAndAlwaysReturnOne() throws Exception {

        final JsonEnvelope event = mock(JsonEnvelope.class);

        assertThat(transactionalEventProcessor.processWithEventBuffer(event), is(1));

        verify(eventBufferProcessor).processWithEventBuffer(event);
    }
}
