package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingException;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.subscription.SubscriptionManager;

import java.util.UUID;

import javax.transaction.Transactional;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private final EventBufferProcessor eventBufferProcessor;
    private final StreamProcessingFailureHandler streamProcessingFailureHandler;
    private final String componentName;

    public DefaultSubscriptionManager(
            final EventBufferProcessor eventBufferProcessor,
            final StreamProcessingFailureHandler streamProcessingFailureHandler,
            final String componentName) {
        this.eventBufferProcessor = eventBufferProcessor;
        this.streamProcessingFailureHandler = streamProcessingFailureHandler;
        this.componentName = componentName;
    }

    @Transactional(REQUIRED)
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {
        try {
            eventBufferProcessor.processWithEventBuffer(incomingJsonEnvelope);
            streamProcessingFailureHandler.onStreamProcessingSucceeded(incomingJsonEnvelope, componentName);
        } catch (final Throwable e) {
            streamProcessingFailureHandler.onStreamProcessingFailure(incomingJsonEnvelope, e, componentName);
            final Metadata metadata = incomingJsonEnvelope.metadata();
            throw new StreamProcessingException(format("Failed to process event. name: '%s', eventId: '%s, streamId: '%s'", metadata.name(), metadata.id(), metadata.streamId().orElse(null)), e);
        }
    }
}
