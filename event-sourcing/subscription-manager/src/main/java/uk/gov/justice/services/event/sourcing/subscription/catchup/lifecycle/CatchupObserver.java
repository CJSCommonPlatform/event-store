package uk.gov.justice.services.event.sourcing.subscription.catchup.lifecycle;

import static java.lang.String.format;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedForSubscriptionEvent;

import java.time.Duration;
import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class CatchupObserver {

    @Inject
    private CatchupsInProgressCache catchupsInProgressCache;

    @Inject
    private CatchupDurationCalculator catchupDurationCalculator;

    @Inject
    private Event<CatchupCompletedEvent> catchupCompletedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void onCatchupStarted(@Observes final CatchupStartedEvent catchupStartedEvent) {
        logger.info("Event catchup started at " + catchupStartedEvent.getCatchupStartedAt());
        logger.info("Performing catchup of events...");

        catchupsInProgressCache.removeAll();
    }

    public void onCatchupStartedForSubscription(@Observes final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {

        final String subscriptionName = catchupStartedForSubscriptionEvent.getSubscriptionName();
        final ZonedDateTime catchupStartedAt = catchupStartedForSubscriptionEvent.getCatchupStartedAt();

        catchupsInProgressCache.addCatchupInProgress(new CatchupInProgress(subscriptionName, catchupStartedAt));

        logger.info(format("Event catchup for subscription '%s' started at %s", subscriptionName, catchupStartedAt));
    }

    public void onCatchupCompleteForSubscription(@Observes final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        final String subscriptionName = catchupCompletedForSubscriptionEvent.getSubscriptionName();
        final ZonedDateTime catchupCompletedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt();
        final int totalNumberOfEvents = catchupCompletedForSubscriptionEvent.getTotalNumberOfEvents();

        logger.info(format("Event catchup for subscription '%s' completed at %s", subscriptionName, catchupCompletedAt));
        logger.info(format("Event catchup for subscription '%s' caught up %d events", subscriptionName, totalNumberOfEvents));

        final CatchupInProgress catchupInProgress = catchupsInProgressCache.removeCatchupInProgress(subscriptionName);

        final Duration catchupDuration = catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent);

        logger.info(format("Event catchup for subscription '%s' took %d milliseconds", subscriptionName, catchupDuration.toMillis()));

        if(catchupsInProgressCache.noCatchupsInProgress()) {
            final ZonedDateTime completedAt = clock.now();
            catchupCompletedEventFirer.fire(new CatchupCompletedEvent(completedAt));
            logger.info(format("Event catchup completed at %s", completedAt));
        }
    }
}
