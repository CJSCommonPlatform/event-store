package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.event.sourcing.subscription.manager.BackwardsCompatibleSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import javax.inject.Inject;

public class BackwardsCompatibleSubscriptionManagerFactory {

    @Inject
    private InterceptorContextProvider interceptorContextProvider;

    @Inject
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    private StreamProcessingFailureHandler streamProcessingFailureHandler;

    public BackwardsCompatibleSubscriptionManager create(final String componentName) {

        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer
                .produceLocalProcessor(componentName);

        return new BackwardsCompatibleSubscriptionManager(
                interceptorChainProcessor,
                interceptorContextProvider,
                streamProcessingFailureHandler,
                componentName);
    }
}
