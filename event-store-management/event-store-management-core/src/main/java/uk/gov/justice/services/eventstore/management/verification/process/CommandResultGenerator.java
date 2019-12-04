package uk.gov.justice.services.eventstore.management.verification.process;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.CommandResult.failure;
import static uk.gov.justice.services.eventstore.management.CommandResult.success;

import uk.gov.justice.services.eventstore.management.CommandResult;
import uk.gov.justice.services.eventstore.management.commands.VerificationCommand;

import java.util.List;
import java.util.UUID;

public class CommandResultGenerator {

    public CommandResult createCommandResult(
            final UUID commandId,
            final VerificationCommand verificationCommand,
            final List<VerificationResult> successfulResults,
            final List<VerificationResult> warningResults,
            final List<VerificationResult> errorResults) {


        if (errorResults.isEmpty()) {
            final String message = format(
                    "%s completed successfully with %d Error(s), %d Warning(s) and %d Success(es)",
                    verificationCommand.getName(),
                    errorResults.size(),
                    warningResults.size(),
                    successfulResults.size());

            return success(commandId, message);
        }

        final String message = format(
                "%s failed with %d Error(s), %d Warning(s) and %d Success(es)",
                verificationCommand.getName(),
                errorResults.size(),
                warningResults.size(),
                successfulResults.size());

        return failure(
                commandId,
                message
        );
    }
}
