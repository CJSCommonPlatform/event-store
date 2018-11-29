package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.BackwardsCompatibleSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.DefaultSubscriptionManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferSelector;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.Optional;

import javax.inject.Inject;

public class SubscriptionManagerFactory {

    @Inject
    SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry;

    @Inject
    EventSourceProvider eventSourceProvider;

    @Inject
    EventBufferProcessorFactory eventBufferProcessorFactory;

    @Inject
    EventBufferSelector eventBufferSelector;

    @Inject
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @Inject
    InterceptorContextProvider interceptorContextProvider;

    public SubscriptionManager create(final Subscription subscription) {

        final String subscriptionName = subscription.getName();
        final String eventSourceName = subscription.getEventSourceName();

        final String componentName = subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName);
        final EventSource eventSource = eventSourceProvider.getEventSource(eventSourceName);

        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer.produceLocalProcessor(componentName);
        final Optional<EventBufferService> selectedEventBufferService = eventBufferSelector.selectFor(componentName);

        return selectedEventBufferService
                .map(eventBufferService -> defaultSubscriptionManager(
                        subscription,
                        eventSource,
                        interceptorChainProcessor,
                        eventBufferService))
                .orElseGet(() -> new BackwardsCompatibleSubscriptionManager(interceptorChainProcessor, interceptorContextProvider));
    }

    private SubscriptionManager defaultSubscriptionManager(
            final Subscription subscription,
            final EventSource eventSource,
            final InterceptorChainProcessor interceptorChainProcessor,
            final EventBufferService eventBufferService) {

        final EventBufferProcessor eventBufferProcessor = eventBufferProcessorFactory.create(
                interceptorChainProcessor,
                eventBufferService,
                interceptorContextProvider);

        return new DefaultSubscriptionManager(
                eventBufferProcessor,
                eventCatchupProcessorFactory.create(subscription, eventSource, eventBufferProcessor)
        );
    }
}
