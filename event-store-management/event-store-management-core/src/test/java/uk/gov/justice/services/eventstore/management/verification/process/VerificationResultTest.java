package uk.gov.justice.services.eventstore.management.verification.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.WARNING;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.error;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.success;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.warning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VerificationResultTest {

    @Test
    public void shouldCreateSuccessResult() throws Exception {

        final String message = "message";

        final VerificationResult verificationResult = success(message);

        assertThat(verificationResult.getVerificationResultType(), is(SUCCESS));
        assertThat(verificationResult.getMessage(), is(message));
    }

    @Test
    public void shouldCreateWarningResult() throws Exception {

        final String message = "message";

        final VerificationResult verificationResult = warning(message);

        assertThat(verificationResult.getVerificationResultType(), is(WARNING));
        assertThat(verificationResult.getMessage(), is(message));
    }

    @Test
    public void shouldCreateErrorResult() throws Exception {

        final String message = "message";

        final VerificationResult verificationResult = error(message);

        assertThat(verificationResult.getVerificationResultType(), is(ERROR));
        assertThat(verificationResult.getMessage(), is(message));
    }
}
