package uk.gov.justice.services.event.source.subscriptions.interceptors;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.inject.Inject;

public class SubscriptionEventInterceptor implements Interceptor {

    @Inject
    ProcessedEventTrackingService processedEventTrackingService;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {

        final InterceptorContext resultInterceptorContext = interceptorChain.processNext(interceptorContext);

        processedEventTrackingService.trackProcessedEvent(resultInterceptorContext.inputEnvelope());

        return resultInterceptorContext;
    }
}
