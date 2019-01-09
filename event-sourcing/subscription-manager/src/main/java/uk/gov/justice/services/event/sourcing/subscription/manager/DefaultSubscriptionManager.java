package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.SubscriptionManager;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private final EventBufferProcessor eventBufferProcessor;

    public DefaultSubscriptionManager(final EventBufferProcessor eventBufferProcessor) {
        this.eventBufferProcessor = eventBufferProcessor;
    }

    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {
        eventBufferProcessor.processWithEventBuffer(incomingJsonEnvelope);
    }
}
