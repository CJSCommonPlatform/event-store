package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.WARNING;

import java.util.List;

public class VerificationResultFilter {

    public List<VerificationResult> findSuccesses(final List<VerificationResult> verificationResults) {
        return filter(SUCCESS, verificationResults);
    }

    public List<VerificationResult> findWarnings(final List<VerificationResult> verificationResults) {
        return filter(WARNING, verificationResults);
    }

    public List<VerificationResult> findErrors(final List<VerificationResult> verificationResults) {
        return filter(ERROR, verificationResults);
    }

    private List<VerificationResult> filter(
            final VerificationResult.VerificationResultType verificationResultType,
            final List<VerificationResult> verificationResults) {

        return verificationResults.stream()
                .filter(verificationResult -> verificationResult.getVerificationResultType() == verificationResultType)
                .collect(toList());
    }
}
