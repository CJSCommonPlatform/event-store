package uk.gov.justice.services.eventstore.management.validation.process;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult.failure;
import static uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult.success;

import uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult;

import java.util.List;
import java.util.UUID;

public class CommandResultGenerator {

    public VerificationCommandResult createCommandResult(
            final UUID commandId,
            final List<VerificationResult> successfulResults,
            final List<VerificationResult> warningResults,
            final List<VerificationResult> errorResults) {


        if (errorResults.isEmpty()) {
            final String message = format(
                    "Verification of Catchup completed successfully with %d Error(s), %d Warning(s) and %d Success(es)",
                    errorResults.size(),
                    warningResults.size(),
                    successfulResults.size());

            return success(commandId, message);
        }

        final String message = format(
                "Verification of Catchup failed with %d Error(s), %d Warning(s) and %d Success(es)",
                errorResults.size(),
                warningResults.size(),
                successfulResults.size());

        return failure(
                commandId,
                message
        );

    }
}
