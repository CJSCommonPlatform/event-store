package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.lang.String.format;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType;
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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupLifecycle {

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


    public void handleCatchupRequested(final CatchupRequestedEvent catchupRequestedEvent) {

        final CatchupType catchupType = catchupRequestedEvent.getCatchupType();
        final SystemCommand target = catchupRequestedEvent.getTarget();

        logger.info(format("%s catchup requested", catchupType.getName()));

        eventCatchupRunner.runEventCatchup(catchupType, target);
    }

    public void handleCatchupStarted(final CatchupStartedEvent catchupStartedEvent) {

        final CatchupType catchupType = catchupStartedEvent.getCatchupType();
        final ZonedDateTime catchupStartedAt = catchupStartedEvent.getCatchupStartedAt();

        logger.info(format("%s catchup started at %s", catchupType.getName(), catchupStartedAt));

        catchupsInProgressCache.removeAll(catchupType);
    }

    public void handleCatchupStartedForSubscription(final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {

        final String subscriptionName = catchupStartedForSubscriptionEvent.getSubscriptionName();
        final ZonedDateTime catchupStartedAt = catchupStartedForSubscriptionEvent.getCatchupStartedAt();
        final CatchupType catchupType = catchupStartedForSubscriptionEvent.getCatchupType();

        catchupsInProgressCache.addCatchupInProgress(new CatchupInProgress(subscriptionName, catchupStartedAt), catchupType);

        logger.info(format("%s catchup for subscription '%s' started at %s", catchupType.getName(), subscriptionName, catchupStartedAt));
    }

    public void handleCatchupCompleteForSubscription(final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        final String subscriptionName = catchupCompletedForSubscriptionEvent.getSubscriptionName();

        final ZonedDateTime catchupCompletedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt();
        final int totalNumberOfEvents = catchupCompletedForSubscriptionEvent.getTotalNumberOfEvents();
        final CatchupType catchupType = catchupCompletedForSubscriptionEvent.getCatchupType();

        logger.info(format("%s catchup for subscription '%s' completed at %s", catchupType.getName(), subscriptionName, catchupCompletedAt));
        logger.info(format("%s catchup for subscription '%s' caught up %d events", catchupType.getName(), subscriptionName, totalNumberOfEvents));

        final CatchupInProgress catchupInProgress = catchupsInProgressCache.removeCatchupInProgress(subscriptionName, catchupType);

        final Duration catchupDuration = catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent);

        logger.info(format("%s catchup for subscription '%s' took %d milliseconds", catchupType.getName(), subscriptionName, catchupDuration.toMillis()));

        if (catchupsInProgressCache.noCatchupsInProgress(catchupType)) {
            final ZonedDateTime completedAt = clock.now();
            final SystemCommand target = catchupCompletedForSubscriptionEvent.getTarget();
            catchupCompletedEventFirer.fire(new CatchupCompletedEvent(target, completedAt));
            logger.info(format("%s catchup fully complete at %s", catchupType.getName(), completedAt));
        }
    }
}
