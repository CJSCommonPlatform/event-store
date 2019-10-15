package uk.gov.justice.services.eventstore.management.catchup.observers;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

@Interceptors(MdcLoggerInterceptor.class)
@ApplicationScoped
public class CatchupObserver {

    @Inject
    private CatchupLifecycle catchupLifecycle;

    public void onCatchupRequested(@Observes final CatchupRequestedEvent catchupRequestedEvent) {
        catchupLifecycle.handleCatchupRequested(catchupRequestedEvent);
    }

    public void onCatchupStartedForSubscription(@Observes final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {
        catchupLifecycle.handleCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);
    }

    public void onCatchupCompleteForSubscription(@Observes final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {
        catchupLifecycle.handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);
    }

    public void onCatchupProcessingOfEventFailed(@Observes final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent) {
        catchupLifecycle.handleCatchupProcessingOfEventFailed(catchupProcessingOfEventFailedEvent);
    }
}
