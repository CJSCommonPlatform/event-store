package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.lang.String.format;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupRunner;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupError;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupErrorStateManager;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupStateManager;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupLifecycle {

    @Inject
    private EventCatchupRunner eventCatchupRunner;

    @Inject
    private CatchupStateManager catchupStateManager;

    @Inject
    private CatchupErrorStateManager catchupErrorStateManager;

    @Inject
    private CatchupDurationCalculator catchupDurationCalculator;

    @Inject
    private Event<CatchupCompletedEvent> catchupCompletedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void handleCatchupRequested(final CatchupRequestedEvent catchupRequestedEvent) {

        final UUID commandId = catchupRequestedEvent.getCommandId();
        final CatchupType catchupType = catchupRequestedEvent.getCatchupType();
        final SystemCommand target = catchupRequestedEvent.getTarget();

        logger.info(format("%s catchup requested", catchupType.getName()));

        catchupStateManager.clear(catchupType);
        catchupErrorStateManager.clear(catchupType);

        eventCatchupRunner.runEventCatchup(commandId, catchupType, target);
    }

    public void handleCatchupStarted(final CatchupStartedEvent catchupStartedEvent) {

        final CatchupType catchupType = catchupStartedEvent.getCatchupType();
        final ZonedDateTime catchupStartedAt = catchupStartedEvent.getCatchupStartedAt();

        logger.info(format("%s catchup started at %s", catchupType.getName(), catchupStartedAt));
    }

    public void handleCatchupStartedForSubscription(final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {

        final String subscriptionName = catchupStartedForSubscriptionEvent.getSubscriptionName();
        final ZonedDateTime catchupStartedAt = catchupStartedForSubscriptionEvent.getCatchupStartedAt();
        final CatchupType catchupType = catchupStartedForSubscriptionEvent.getCatchupType();

        catchupStateManager.addCatchupInProgress(new CatchupInProgress(subscriptionName, catchupStartedAt), catchupType);

        logger.info(format("%s catchup for subscription '%s' started at %s", catchupType.getName(), subscriptionName, catchupStartedAt));
    }

    public void handleCatchupCompleteForSubscription(final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        final UUID commandId = catchupCompletedForSubscriptionEvent.getCommandId();
        final String subscriptionName = catchupCompletedForSubscriptionEvent.getSubscriptionName();

        final ZonedDateTime catchupCompletedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt();
        final int totalNumberOfEvents = catchupCompletedForSubscriptionEvent.getTotalNumberOfEvents();
        final CatchupType catchupType = catchupCompletedForSubscriptionEvent.getCatchupType();

        logger.info(format("%s catchup for subscription '%s' completed at %s", catchupType.getName(), subscriptionName, catchupCompletedAt));
        logger.info(format("%s catchup for subscription '%s' caught up %d events", catchupType.getName(), subscriptionName, totalNumberOfEvents));

        final CatchupInProgress catchupInProgress = catchupStateManager.removeCatchupInProgress(subscriptionName, catchupType);

        final Duration catchupDuration = catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent);

        logger.info(format("%s catchup for subscription '%s' took %d milliseconds", catchupType.getName(), subscriptionName, catchupDuration.toMillis()));

        if (catchupStateManager.noCatchupsInProgress(catchupType)) {
            final SystemCommand target = catchupCompletedForSubscriptionEvent.getTarget();
            final ZonedDateTime completedAt = clock.now();

            catchupCompletedEventFirer.fire(new CatchupCompletedEvent(
                    commandId,
                    target,
                    completedAt,
                    catchupType));
        }
    }

    public void handleCatchupComplete(final CatchupCompletedEvent catchupCompletedEvent) {

        final CatchupType catchupType = catchupCompletedEvent.getCatchupType();
        final ZonedDateTime completedAt = catchupCompletedEvent.getCompletedAt();

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(catchupType);
        if (errors.isEmpty()) {
            logger.info(format("%s catchup successfully completed with 0 errors at %s", catchupType.getName(), completedAt));
        } else {
            logger.error(format("%s catchup failed with %d errors", catchupType.getName(), errors.size()));
        }
    }

    public void handleCatchupProcessingOfEventFailed(final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent) {

        final CatchupType catchupType = catchupProcessingOfEventFailedEvent.getCatchupType();

        final CatchupError catchupError = new CatchupError(
                catchupProcessingOfEventFailedEvent.getEventId(),
                catchupProcessingOfEventFailedEvent.getMetadata(),
                catchupProcessingOfEventFailedEvent.getSubscriptionName(),
                catchupType, catchupProcessingOfEventFailedEvent.getException()
        );

        catchupErrorStateManager.add(catchupError, catchupType);
    }
}
