package uk.gov.justice.services.event.source.subscriptions.interceptors;

import static java.lang.String.format;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SubscriptionEventInterceptor implements Interceptor {

    @Inject
    SubscriptionsRepository subscriptionsRepository;

    @Inject
    Logger logger;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {

        final InterceptorContext resultInterceptorContext = interceptorChain.processNext(interceptorContext);
        final Metadata metadata = resultInterceptorContext.inputEnvelope().metadata();

        final Optional<Long> eventNumber = metadata.eventNumber();
        final Optional<String> source = metadata.source();

        if (eventNumber.isPresent() && source.isPresent()) {
            subscriptionsRepository.insertOrUpdateCurrentEventNumber(eventNumber.get(), source.get());
        } else {
            if (!eventNumber.isPresent()) {
                logger.warn(format("Event with name %s has no event number.", metadata.name()));
            }

            if (!source.isPresent()) {
                logger.warn(format("Event with name %s has no source.", metadata.name()));
            }
        }

        return resultInterceptorContext;
    }

}
