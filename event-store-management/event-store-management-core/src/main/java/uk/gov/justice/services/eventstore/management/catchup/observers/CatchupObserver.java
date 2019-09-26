package uk.gov.justice.services.eventstore.management.catchup.observers;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.logging.MdcLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class CatchupObserver {

    @Inject
    private CatchupLifecycle catchupLifecycle;

    @Inject
    private MdcLogger mdcLogger;

    public void onCatchupRequested(@Observes final CatchupRequestedEvent catchupRequestedEvent) {

        mdcLogger
                .mdcLoggerConsumer()
                .accept(() -> catchupLifecycle.handleCatchupRequested(catchupRequestedEvent));
    }

    public void onCatchupStarted(@Observes final CatchupStartedEvent catchupStartedEvent) {

        mdcLogger
                .mdcLoggerConsumer()
                .accept(() -> catchupLifecycle.handleCatchupStarted(catchupStartedEvent));
    }

    public void onCatchupStartedForSubscription(@Observes final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {

        mdcLogger
                .mdcLoggerConsumer()
                .accept(() -> catchupLifecycle.handleCatchupStartedForSubscription(catchupStartedForSubscriptionEvent));
    }

    public void onCatchupCompleteForSubscription(@Observes final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        mdcLogger
                .mdcLoggerConsumer()
                .accept(() -> catchupLifecycle.handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent));
    }
}
