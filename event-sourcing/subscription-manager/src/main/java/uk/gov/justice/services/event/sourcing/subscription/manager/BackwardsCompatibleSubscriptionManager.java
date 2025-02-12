package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
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
    private final String componentName;

    public BackwardsCompatibleSubscriptionManager(
            final InterceptorChainProcessor interceptorChainProcessor,
            final InterceptorContextProvider interceptorContextProvider,
            final StreamProcessingFailureHandler streamProcessingFailureHandler,
            final String componentName) {
        this.interceptorChainProcessor = interceptorChainProcessor;
        this.interceptorContextProvider = interceptorContextProvider;
        this.streamProcessingFailureHandler = streamProcessingFailureHandler;
        this.componentName = componentName;
    }

    @Transactional(REQUIRED)
    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {

        final InterceptorContext interceptorContext = interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope);
        try {
            interceptorChainProcessor.process(interceptorContext);
            streamProcessingFailureHandler.onStreamProcessingSucceeded(incomingJsonEnvelope, componentName);
        } catch (final Throwable e) {
            streamProcessingFailureHandler.onStreamProcessingFailure(incomingJsonEnvelope, e, componentName);
            final Metadata metadata = incomingJsonEnvelope.metadata();
            throw new StreamProcessingException(format("Failed to process event. name: '%s', eventId: '%s, streamId: '%s'", metadata.name(), metadata.id(), metadata.streamId().orElse(null)), e);
        }
    }
}
