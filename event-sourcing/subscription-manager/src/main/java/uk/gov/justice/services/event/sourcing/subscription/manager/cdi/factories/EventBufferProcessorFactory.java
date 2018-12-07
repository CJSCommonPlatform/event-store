package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;

public class EventBufferProcessorFactory {

    public EventBufferProcessor create(
            final InterceptorChainProcessor interceptorChainProcessor,
            final EventBufferService eventBufferService,
            final InterceptorContextProvider interceptorContextProvider) {

        return new EventBufferProcessor(interceptorChainProcessor, eventBufferService, interceptorContextProvider);
    }
}
