package uk.gov.justice.services.event.sourcing.subscription.manager;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.transaction.Transactional;

public class TransactionalEventProcessor {

    private final EventBufferProcessor eventBufferProcessor;

    public TransactionalEventProcessor(final EventBufferProcessor eventBufferProcessor) {
        this.eventBufferProcessor = eventBufferProcessor;
    }

    @Transactional(REQUIRES_NEW)
    public int processWithEventBuffer(final JsonEnvelope event) {
        eventBufferProcessor.processWithEventBuffer(event);
        return 1;
    }
}
