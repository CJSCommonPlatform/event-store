package uk.gov.justice.services.eventstore.management.replay.process;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

import javax.inject.Inject;

public class EventBufferProcessorFactory {

    @Inject
    private EventBufferService eventBufferService;

    @Inject
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    private InterceptorContextProvider interceptorContextProvider;

    public EventBufferProcessor create(String componentName) {
        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer.produceLocalProcessor(componentName);
        return new EventBufferProcessor(interceptorChainProcessor, eventBufferService, interceptorContextProvider, componentName);
    }
}
