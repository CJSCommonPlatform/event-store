package uk.gov.justice.services.event.sourcing.subscription.manager.cdi;

import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

public class InterceptorContextProvider {

    public InterceptorContext getInterceptorContext(final JsonEnvelope incomingJsonEnvelope) {
        return interceptorContextWithInput(incomingJsonEnvelope);
    }
}
