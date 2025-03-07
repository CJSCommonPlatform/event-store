package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamErrorRepository;
import uk.gov.justice.services.event.sourcing.subscription.error.StreamProcessingFailureHandler;
import uk.gov.justice.services.event.sourcing.subscription.manager.DefaultSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.subscription.SubscriptionManager;

import javax.inject.Inject;

public class DefaultSubscriptionManagerFactory {

    @Inject
    private InterceptorContextProvider interceptorContextProvider;

    @Inject
    private EventBufferService eventBufferService;

    @Inject
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    private StreamProcessingFailureHandler streamProcessingFailureHandler;

    @Inject
    private StreamErrorRepository streamErrorRepository;

    public SubscriptionManager create(final String componentName) {

        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer
                .produceLocalProcessor(componentName);

        final EventBufferProcessor eventBufferProcessor = new EventBufferProcessor(
                interceptorChainProcessor,
                eventBufferService,
                streamErrorRepository,
                interceptorContextProvider,
                componentName);

        return new DefaultSubscriptionManager(eventBufferProcessor, streamProcessingFailureHandler, componentName);
    }
}
