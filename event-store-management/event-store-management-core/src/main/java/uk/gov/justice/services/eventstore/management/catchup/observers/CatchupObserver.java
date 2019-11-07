package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupDurationCalculator;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.catchup.process.EventCatchupRunner;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupError;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupErrorStateManager;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupStateManager;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

@Interceptors(MdcLoggerInterceptor.class)
@ApplicationScoped
public class CatchupObserver {

    @Inject
    private EventCatchupRunner eventCatchupRunner;

    @Inject
    private CatchupStateManager catchupStateManager;

    @Inject
    private CatchupErrorStateManager catchupErrorStateManager;

    @Inject
    private CatchupDurationCalculator catchupDurationCalculator;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private CatchupProcessCompleter catchupProcessCompleter;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void onCatchupRequested(@Observes final CatchupRequestedEvent catchupRequestedEvent) {
        final UUID commandId = catchupRequestedEvent.getCommandId();
        final CatchupCommand catchupCommand = catchupRequestedEvent.getCatchupCommand();

        final ZonedDateTime catchupStartedAt = clock.now();

        catchupStateManager.clear(catchupCommand);
        catchupErrorStateManager.clear(catchupCommand);

        final String message = format("%s started at %s", catchupCommand.getName(), catchupStartedAt);
        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                catchupCommand,
                COMMAND_IN_PROGRESS,
                catchupStartedAt,
                message
        ));

        logger.info(message);

        eventCatchupRunner.runEventCatchup(commandId, catchupCommand);
    }

    public void onCatchupStartedForSubscription(@Observes final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent) {

        final String subscriptionName = catchupStartedForSubscriptionEvent.getSubscriptionName();
        final ZonedDateTime catchupStartedAt = catchupStartedForSubscriptionEvent.getCatchupStartedAt();
        final CatchupCommand catchupCommand = catchupStartedForSubscriptionEvent.getCatchupCommand();

        catchupStateManager.addCatchupInProgress(new CatchupInProgress(subscriptionName, catchupStartedAt), catchupCommand);

        logger.info(format("%s for subscription '%s' started at %s", catchupCommand.getName(), subscriptionName, catchupStartedAt));
    }

    public void onCatchupCompleteForSubscription(@Observes final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent) {
        final UUID commandId = catchupCompletedForSubscriptionEvent.getCommandId();
        final String subscriptionName = catchupCompletedForSubscriptionEvent.getSubscriptionName();

        final ZonedDateTime catchupCompletedAt = catchupCompletedForSubscriptionEvent.getCatchupCompletedAt();
        final int totalNumberOfEvents = catchupCompletedForSubscriptionEvent.getTotalNumberOfEvents();
        final CatchupCommand catchupCommand = catchupCompletedForSubscriptionEvent.getCatchupCommand();

        logger.info(format("%s for subscription '%s' completed at %s", catchupCommand.getName(), subscriptionName, catchupCompletedAt));
        logger.info(format("%s for subscription '%s' caught up %d events", catchupCommand.getName(), subscriptionName, totalNumberOfEvents));

        final CatchupInProgress catchupInProgress = catchupStateManager.removeCatchupInProgress(subscriptionName, catchupCommand);

        final Duration catchupDuration = catchupDurationCalculator.calculate(
                catchupInProgress.getStartedAt(),
                catchupCompletedForSubscriptionEvent.getCatchupCompletedAt());

        logger.info(format("%s for subscription '%s' took %d milliseconds", catchupCommand.getName(), subscriptionName, catchupDuration.toMillis()));

        if (catchupStateManager.noCatchupsInProgress(catchupCommand)) {
            catchupProcessCompleter.handleCatchupComplete(commandId, catchupCommand);
        }
    }

    public void onCatchupProcessingOfEventFailed(@Observes final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent) {
        final CatchupCommand catchupCommand = catchupProcessingOfEventFailedEvent.getCatchupCommand();

        final CatchupError catchupError = new CatchupError(
                catchupProcessingOfEventFailedEvent.getEventId(),
                catchupProcessingOfEventFailedEvent.getMetadata(),
                catchupProcessingOfEventFailedEvent.getSubscriptionName(),
                catchupCommand,
                catchupProcessingOfEventFailedEvent.getException()
        );

        catchupErrorStateManager.add(catchupError, catchupCommand);
    }
}
