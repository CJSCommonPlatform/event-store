package uk.gov.justice.services.eventstore.management.validation.process;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.WARNING;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupVerificationProcess {

    @Inject
    private Logger logger;

    @Inject
    private VerifierProvider verifierProvider;

    public void runVerification() {

        final List<VerificationResult> verificationResults = verifierProvider.getVerifiers().stream()
                .map(Verifier::verify)
                .flatMap(Collection::stream)
                .collect(toList());

        final List<VerificationResult> errorResults = filter(verificationResults, ERROR);
        final List<VerificationResult> warningResults = filter(verificationResults, WARNING);
        final List<VerificationResult> successfulResults = filter(verificationResults, SUCCESS);

        logger.info(format("Verification of Catchup completed with %d Errors, %d Warnings and %d Successes", errorResults.size(), warningResults.size(), successfulResults.size()));

        errorResults.forEach(verificationResult -> logger.error("ERROR: " + verificationResult.getMessage()));
        warningResults.forEach(verificationResult -> logger.warn("WARNING: " + verificationResult.getMessage()));
        successfulResults.forEach(verificationResult -> logger.warn("SUCCESS: " + verificationResult.getMessage()));
    }

    private List<VerificationResult> filter(
            final List<VerificationResult> verificationResults,
            final VerificationResult.VerificationResultType verificationResultType) {

        return verificationResults.stream()
                .filter(verificationResult -> verificationResult.getVerificationResultType() == verificationResultType)
                .collect(toList());
    }
}
