package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingException;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.subscription.SubscriptionManager;

import java.util.UUID;

import javax.transaction.Transactional;

public class BackwardsCompatibleSubscriptionManager implements SubscriptionManager {

    private final InterceptorChainProcessor interceptorChainProcessor;
    private final InterceptorContextProvider interceptorContextProvider;
    private final StreamProcessingFailureHandler streamProcessingFailureHandler;

    public BackwardsCompatibleSubscriptionManager(
            final InterceptorChainProcessor interceptorChainProcessor,
            final InterceptorContextProvider interceptorContextProvider,
            final StreamProcessingFailureHandler streamProcessingFailureHandler) {
        this.interceptorChainProcessor = interceptorChainProcessor;
        this.interceptorContextProvider = interceptorContextProvider;
        this.streamProcessingFailureHandler = streamProcessingFailureHandler;
    }

    @Transactional(REQUIRED)
    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {
        try {
            interceptorChainProcessor.process(interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope));
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
