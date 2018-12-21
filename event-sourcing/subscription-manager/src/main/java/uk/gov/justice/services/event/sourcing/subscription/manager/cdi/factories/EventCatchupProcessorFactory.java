package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import javax.inject.Inject;

public class EventCatchupProcessorFactory {

    @Inject
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    EventBufferService eventBufferService;

    @Inject
    SubscriptionsRepository subscriptionsRepository;

    @Inject
    EventSourceProvider eventSourceProvider;

    @Inject
    InterceptorContextProvider interceptorContextProvider;

    public EventCatchupProcessor createFor(final String componentName) {

        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer
                .produceLocalProcessor(componentName);

        final EventBufferProcessor eventBufferProcessor = new EventBufferProcessor(
                interceptorChainProcessor,
                eventBufferService,
                interceptorContextProvider);

        final TransactionalEventProcessor transactionalEventProcessor = new TransactionalEventProcessor(eventBufferProcessor);

        return new EventCatchupProcessor(
                subscriptionsRepository,
                eventSourceProvider,
                transactionalEventProcessor,
                getLogger(EventCatchupProcessor.class)
        );
    }
}
