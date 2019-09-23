package uk.gov.justice.services.eventstore.management.validation.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.VerificationResultType.WARNING;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.error;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.success;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.warning;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
