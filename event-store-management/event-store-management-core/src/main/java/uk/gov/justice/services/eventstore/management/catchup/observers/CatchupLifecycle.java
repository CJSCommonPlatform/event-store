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
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

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
        final CatchupCommand catchupCommand = catchupRequestedEvent.getCatchupCommand();

        logger.info(format("%s requested", catchupCommand.getName()));

        catchupStateManager.clear(catchupCommand);
        catchupErrorStateManager.clear(catchupCommand);

        eventCatchupRunner.runEventCatchup(commandId, catchupCommand);
    }

    public void handleCatchupStarted(final CatchupStartedEvent catchupStartedEvent) {

        final CatchupCommand catchupCommand = catchupStartedEvent.getCatchupCommand();
        final ZonedDateTime catchupStartedAt = catchupStartedEvent.getCatchupStartedAt();

        logger.info(format("%s started at %s", catchupCommand.getName(), catchupStartedAt));
    }

    public void handleCatchupStartedForSubscription(final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {

        final String subscriptionName = catchupStartedForSubscriptionEvent.getSubscriptionName();
        final ZonedDateTime catchupStartedAt = catchupStartedForSubscriptionEvent.getCatchupStartedAt();
        final CatchupCommand catchupCommand = catchupStartedForSubscriptionEvent.getCatchupCommand();

        catchupStateManager.addCatchupInProgress(new CatchupInProgress(subscriptionName, catchupStartedAt), catchupCommand);

        logger.info(format("%s for subscription '%s' started at %s", catchupCommand.getName(), subscriptionName, catchupStartedAt));
    }

    public void handleCatchupCompleteForSubscription(final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {

        final UUID commandId = catchupCompletedForSubscriptionEvent.getCommandId();
        final String subscriptionName = catchupCompletedForSubscriptionEvent.getSubscriptionName();

        final ZonedDateTime catchupCompletedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt();
        final int totalNumberOfEvents = catchupCompletedForSubscriptionEvent.getTotalNumberOfEvents();
        final CatchupCommand catchupCommand = catchupCompletedForSubscriptionEvent.getCatchupCommand();

        logger.info(format("%s for subscription '%s' completed at %s", catchupCommand.getName(), subscriptionName, catchupCompletedAt));
        logger.info(format("%s for subscription '%s' caught up %d events", catchupCommand.getName(), subscriptionName, totalNumberOfEvents));

        final CatchupInProgress catchupInProgress = catchupStateManager.removeCatchupInProgress(subscriptionName, catchupCommand);

        final Duration catchupDuration = catchupDurationCalculator.calculate(
                catchupInProgress,
                catchupCompletedForSubscriptionEvent);

        logger.info(format("%s for subscription '%s' took %d milliseconds", catchupCommand.getName(), subscriptionName, catchupDuration.toMillis()));

        if (catchupStateManager.noCatchupsInProgress(catchupCommand)) {
            final ZonedDateTime completedAt = clock.now();

            catchupCompletedEventFirer.fire(new CatchupCompletedEvent(
                    commandId,
                    catchupCommand,
                    completedAt));
        }
    }

    public void handleCatchupComplete(final CatchupCompletedEvent catchupCompletedEvent) {

        final CatchupCommand catchupCommand = catchupCompletedEvent.getCatchupCommand();
        final ZonedDateTime completedAt = catchupCompletedEvent.getCompletedAt();

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(catchupCommand);
        if (errors.isEmpty()) {
            logger.info(format("%s successfully completed with 0 errors at %s", catchupCommand.getName(), completedAt));
        } else {
            logger.error(format("%s failed with %d errors", catchupCommand.getName(), errors.size()));
        }
    }

    public void handleCatchupProcessingOfEventFailed(final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent) {

        final CatchupCommand catchupCommand = catchupProcessingOfEventFailedEvent.getCatchupCommand();

        final CatchupError catchupError = new CatchupError(
                catchupProcessingOfEventFailedEvent.getEventId(),
                catchupProcessingOfEventFailedEvent.getMetadata(),
                catchupProcessingOfEventFailedEvent.getSubscriptionName(),
                catchupCommand, catchupProcessingOfEventFailedEvent.getException()
        );

        catchupErrorStateManager.add(catchupError, catchupCommand);
    }
}
