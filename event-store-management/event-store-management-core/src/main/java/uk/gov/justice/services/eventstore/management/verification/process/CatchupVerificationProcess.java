package uk.gov.justice.services.eventstore.management.verification.process;

import uk.gov.justice.services.eventstore.management.CommandResult;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class CatchupVerificationProcess {

    @Inject
    private VerificationRunner verificationRunner;

    @Inject
    private VerificationResultFilter verificationResultFilter;

    @Inject
    private VerificationResultsLogger verificationResultsLogger;

    @Inject
    private CommandResultGenerator commandResultGenerator;

    public CommandResult runVerification(final UUID commandId) {

        final List<VerificationResult> verificationResults = verificationRunner.runVerifiers();

        final List<VerificationResult> successResults = verificationResultFilter.findSuccesses(verificationResults);
        final List<VerificationResult> warningResults = verificationResultFilter.findWarnings(verificationResults);
        final List<VerificationResult> errorResults = verificationResultFilter.findErrors(verificationResults);

        verificationResultsLogger.logResults(
                successResults,
                warningResults,
                errorResults
        );

        return commandResultGenerator.createCommandResult(
                commandId,
                successResults,
                warningResults,
                errorResults
        );
    }
}
