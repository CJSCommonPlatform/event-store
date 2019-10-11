package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupError;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupErrorStateManager;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupProcessCompleter {

    @Inject
    private CatchupErrorStateManager catchupErrorStateManager;

    @Inject
    private UtcClock clock;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private Logger logger;

    public void handleCatchupComplete(final UUID commandId, final CatchupCommand catchupCommand) {

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(catchupCommand);

        if (errors.isEmpty()) {
            completeSuccessfully(commandId, catchupCommand);
        } else {
            fail(commandId, catchupCommand, errors);
        }
    }

    private void completeSuccessfully(final UUID commandId, final CatchupCommand catchupCommand) {
        final ZonedDateTime completedAt = clock.now();
        final String message = format("%s successfully completed with 0 errors at %s", catchupCommand.getName(), completedAt);
        logger.info(message);

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                catchupCommand,
                COMMAND_COMPLETE,
                completedAt,
                message
        ));
    }

    private void fail(final UUID commandId, final CatchupCommand catchupCommand, final List<CatchupError> errors) {
        final ZonedDateTime completedAt = clock.now();
        final String message = format("%s failed with %d errors at %s", catchupCommand.getName(), errors.size(), completedAt);
        logger.error(message);

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                catchupCommand,
                COMMAND_FAILED,
                clock.now(),
                message
        ));
    }
}
