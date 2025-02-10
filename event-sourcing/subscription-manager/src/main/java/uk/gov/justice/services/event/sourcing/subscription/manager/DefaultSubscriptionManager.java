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

    public DefaultSubscriptionManager(final EventBufferProcessor eventBufferProcessor, final StreamProcessingFailureHandler streamProcessingFailureHandler) {
        this.eventBufferProcessor = eventBufferProcessor;
        this.streamProcessingFailureHandler = streamProcessingFailureHandler;
    }

    @Transactional(REQUIRED)
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {
        try {
            eventBufferProcessor.processWithEventBuffer(incomingJsonEnvelope);
        } catch (final Throwable e) {
            streamProcessingFailureHandler.onStreamProcessingFailure(incomingJsonEnvelope, e);
            final Metadata metadata = incomingJsonEnvelope.metadata();
            final String name = metadata.name();
            final UUID id = metadata.id();
            final UUID streamId = metadata.streamId().orElse(null);
            throw new StreamProcessingException(format("Failed to process event. name: '%s', eventId: '%s, streamId: '%s'", name, id, streamId), e);
        }
    }
}
