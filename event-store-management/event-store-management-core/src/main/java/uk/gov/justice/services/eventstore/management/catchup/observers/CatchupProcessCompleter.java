package uk.gov.justice.services.eventstore.management.catchup.observers;

import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;

import uk.gov.justice.services.eventstore.management.catchup.state.CatchupError;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupErrorStateManager;
import uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult;
import uk.gov.justice.services.eventstore.management.validation.process.CatchupVerificationProcess;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class CatchupProcessCompleter {

    @Inject
    private CatchupErrorStateManager catchupErrorStateManager;

    @Inject
    private CatchupVerificationProcess catchupVerificationProcess;

    @Inject
    private CatchupCompletionEventFirer catchupCompletionEventFirer;

    public void handleCatchupComplete(final UUID commandId, final CatchupCommand catchupCommand) {

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(catchupCommand);

        if (errors.isEmpty()) {
            final VerificationCommandResult verificationCommandResult = catchupVerificationProcess.runVerification(commandId);

            if (verificationCommandResult.getCommandState() == COMMAND_COMPLETE) {
                catchupCompletionEventFirer.completeSuccessfully(commandId, catchupCommand);
            } else {
                catchupCompletionEventFirer.failVerification(commandId, catchupCommand, verificationCommandResult);
            }
        } else {
            catchupCompletionEventFirer.failCatchup(commandId, catchupCommand, errors);
        }
    }
}
