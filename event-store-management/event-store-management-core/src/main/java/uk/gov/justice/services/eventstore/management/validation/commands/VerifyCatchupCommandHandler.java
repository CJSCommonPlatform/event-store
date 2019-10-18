package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand.VERIFY_CATCHUP;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.validation.process.CatchupVerificationProcess;
import uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

public class VerifyCatchupCommandHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private CatchupVerificationProcess catchupVerificationProcess;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(VERIFY_CATCHUP)
    public void validateCatchup(final VerifyCatchupCommand verifyCatchupCommand, final UUID commandId) {

        logger.info(format("Received %s command", verifyCatchupCommand.getName()));

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                verifyCatchupCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                "Verification of catchup started"
        ));

        try {
            final VerificationCommandResult verificationCommandResult = catchupVerificationProcess.runVerification(commandId);

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    verifyCatchupCommand,
                    verificationCommandResult.getCommandState(),
                    clock.now(),
                    verificationCommandResult.getMessage()
            ));
        } catch (final Exception e) {
            final String message = format("Verification of catchup failed: %s: %s", e.getClass().getSimpleName(), e.getMessage());

            logger.error(message, e);

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    verifyCatchupCommand,
                    COMMAND_FAILED,
                    clock.now(),
                    message
            ));
        }
    }
}
