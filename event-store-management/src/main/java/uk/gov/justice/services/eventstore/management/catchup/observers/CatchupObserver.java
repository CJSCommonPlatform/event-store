package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.lang.String.format;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupsInProgressCache;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupRunner;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

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
    private EventCatchupRunner eventCatchupRunner;

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

    public void onCatchupRequested(@SuppressWarnings("unused") @Observes final CatchupRequestedEvent catchupRequestedEvent) {
        logger.info("Event catchup requested");
        eventCatchupRunner.runEventCatchup(catchupRequestedEvent);
    }

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
            final SystemCommand target = catchupCompletedForSubscriptionEvent.getTarget();
            catchupCompletedEventFirer.fire(new CatchupCompletedEvent(target, completedAt));
            logger.info(format("Event catchup fully complete at %s", completedAt));
        }
    }
}
