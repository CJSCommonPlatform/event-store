package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.SubscriptionManager;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private final EventBufferProcessor eventBufferProcessor;
    private final EventCatchupProcessor eventCatchupProcessor;

    public DefaultSubscriptionManager(final EventBufferProcessor eventBufferProcessor,
                                      final EventCatchupProcessor eventCatchupProcessor) {
        this.eventBufferProcessor = eventBufferProcessor;
        this.eventCatchupProcessor = eventCatchupProcessor;
    }

    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {
        eventBufferProcessor.processWithEventBuffer(incomingJsonEnvelope);
    }

    @Override
    public void startSubscription() {
        eventCatchupProcessor.performEventCatchup();
    }
}
