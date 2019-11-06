package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.commands.ValidatePublishedEventsCommand.VALIDATE_EVENTS;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.CommandResult;
import uk.gov.justice.services.eventstore.management.commands.ValidatePublishedEventsCommand;
import uk.gov.justice.services.eventstore.management.validation.process.EventValidationProcess;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

public class ValidatePublishedEventCommandHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private EventValidationProcess eventValidationProcess;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(VALIDATE_EVENTS)
    public void validateEventsAgainstSchemas(final ValidatePublishedEventsCommand validatePublishedEventsCommand, final UUID commandId) {

        logger.info(format("Received %s command", validatePublishedEventsCommand.getName()));

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                validatePublishedEventsCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                "Validation of PublishedEvents against their schemas started"
        ));

        try {
            final CommandResult commandResult = eventValidationProcess.validateAllPublishedEvents(commandId);

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    validatePublishedEventsCommand,
                    commandResult.getCommandState(),
                    clock.now(),
                    commandResult.getMessage()
            ));
        } catch (final Exception e) {
            final String message = format("Validation of PublishedEvents failed: %s: %s", e.getClass().getSimpleName(), e.getMessage());

            logger.error(message, e);

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    validatePublishedEventsCommand,
                    COMMAND_FAILED,
                    clock.now(),
                    message
            ));
        }
    }
}
