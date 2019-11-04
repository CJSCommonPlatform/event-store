package uk.gov.justice.services.eventstore.management.verification.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class VerificationResultsLoggerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private VerificationResultsLogger verificationResultsLogger;

    @Test
    public void shouldLogAllSuccessesWarningsAndErrors() throws Exception {

        final VerificationResult successfulResult_1 = mock(VerificationResult.class);
        final VerificationResult successfulResult_2 = mock(VerificationResult.class);
        final VerificationResult warningResult_1 = mock(VerificationResult.class);
        final VerificationResult warningResult_2 = mock(VerificationResult.class);
        final VerificationResult errorResult_1 = mock(VerificationResult.class);
        final VerificationResult errorResult_2 = mock(VerificationResult.class);

        final List<VerificationResult> successResults = asList(successfulResult_1, successfulResult_2);
        final List<VerificationResult> warningResults = asList(warningResult_1, warningResult_2);
        final List<VerificationResult> errorResults = asList(errorResult_1, errorResult_2);

        when(successfulResult_1.getMessage()).thenReturn("Success 1");
        when(successfulResult_2.getMessage()).thenReturn("Success 2");
        when(warningResult_1.getMessage()).thenReturn("Warning 1");
        when(warningResult_2.getMessage()).thenReturn("Warning 2");
        when(errorResult_1.getMessage()).thenReturn("Error 1");
        when(errorResult_2.getMessage()).thenReturn("Error 2");

        verificationResultsLogger.logResults(successResults, warningResults, errorResults);

        final InOrder inOrder = inOrder(logger);

        inOrder.verify(logger).info("Verification of Catchup completed with 2 Errors, 2 Warnings and 2 Successes");
        inOrder.verify(logger).error("ERROR: Error 1");
        inOrder.verify(logger).error("ERROR: Error 2");
        inOrder.verify(logger).warn("WARNING: Warning 1");
        inOrder.verify(logger).warn("WARNING: Warning 2");
        inOrder.verify(logger).info("SUCCESS: Success 1");
        inOrder.verify(logger).info("SUCCESS: Success 2");

    }
}
