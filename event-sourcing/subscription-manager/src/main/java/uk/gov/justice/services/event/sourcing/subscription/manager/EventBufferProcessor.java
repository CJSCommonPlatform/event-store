package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.InterceptorContextProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

public class EventBufferProcessor {

    private final InterceptorChainProcessor interceptorChainProcessor;
    private final EventBufferService eventBufferService;
    private final InterceptorContextProvider interceptorContextProvider;
    private final String component;

    public EventBufferProcessor(
            final InterceptorChainProcessor interceptorChainProcessor,
            final EventBufferService eventBufferService,
            final InterceptorContextProvider interceptorContextProvider,
            final String component) {
        this.interceptorChainProcessor = interceptorChainProcessor;
        this.eventBufferService = eventBufferService;
        this.interceptorContextProvider = interceptorContextProvider;
        this.component = component;
    }

    public void processWithEventBuffer(final JsonEnvelope incomingJsonEnvelope) {
        try (final Stream<JsonEnvelope> jsonEnvelopeStream = eventBufferService.currentOrderedEventsWith(incomingJsonEnvelope, component)) {
            jsonEnvelopeStream.forEach(incomingJsonEnvelope1 -> {
                final InterceptorContext interceptorContext = interceptorContextProvider.getInterceptorContext(incomingJsonEnvelope1);
                interceptorChainProcessor.process(interceptorContext);
            });
        }
    }
}

