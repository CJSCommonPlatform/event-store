package uk.gov.justice.services.eventstore.management.validation.process;

import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.WARNING;

public class VerificationResult {

    enum VerificationResultType {
        SUCCESS,
        WARNING,
        ERROR
    }

    private final VerificationResultType verificationResultType;
    private final String message;

    private VerificationResult(final VerificationResultType verificationResultType, final String message) {
        this.verificationResultType = verificationResultType;
        this.message = message;
    }

    public static VerificationResult success(final String message) {
        return new VerificationResult(SUCCESS, message);
    }

    public static VerificationResult warning(final String message) {
        return new VerificationResult(WARNING, message);
    }

    public static VerificationResult error(final String message) {
        return new VerificationResult(ERROR, message);
    }

    public VerificationResultType getVerificationResultType() {
        return verificationResultType;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "VerificationResult{" +
                "verificationResultType=" + verificationResultType +
                ", message='" + message + '\'' +
                '}';
    }
}
