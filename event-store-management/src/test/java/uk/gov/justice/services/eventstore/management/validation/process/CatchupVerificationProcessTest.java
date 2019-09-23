package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.error;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.success;
import static uk.gov.justice.services.eventstore.management.validation.process.VerificationResult.warning;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CatchupVerificationProcessTest {

    @Mock
    private Logger logger;

    @Mock
    private VerifierProvider verifierProvider;

    @InjectMocks
    private CatchupVerificationProcess catchupVerificationProcess;

    @Test
    public void shouldRunTheVariousVerificationProcessesAndLogTheResults() throws Exception {

        final VerificationResult error_1 = error("error 1");
        final VerificationResult error_2 = error("error 2");
        final VerificationResult warning_1 = warning("warning 1");
        final VerificationResult warning_2 = warning("warning 2");
        final VerificationResult success_1 = success("success 1");
        final VerificationResult success_2 = success("success 2");

        final Verifier verifier_1 = mock(Verifier.class);
        final Verifier verifier_2 = mock(Verifier.class);

        when(verifierProvider.getVerifiers()).thenReturn(asList(verifier_1, verifier_2));
        when(verifier_1.verify()).thenReturn(asList(warning_1, success_1, error_1));
        when(verifier_2.verify()).thenReturn(asList(success_2, warning_2, error_2));

        catchupVerificationProcess.runVerification();

        final InOrder inOrder = inOrder(logger);

        inOrder.verify(logger).info("Verification of Catchup completed with 2 Errors, 2 Warnings and 2 Successes");

        inOrder.verify(logger).error("ERROR: error 1");
        inOrder.verify(logger).error("ERROR: error 2");
        inOrder.verify(logger).warn("WARNING: warning 1");
        inOrder.verify(logger).warn("WARNING: warning 2");
        inOrder.verify(logger).warn("SUCCESS: success 1");
        inOrder.verify(logger).warn("SUCCESS: success 2");
    }
}
