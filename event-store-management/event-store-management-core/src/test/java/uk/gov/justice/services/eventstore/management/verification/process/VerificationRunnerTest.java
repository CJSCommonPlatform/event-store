package uk.gov.justice.services.eventstore.management.verification.process;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.commands.VerificationCommand;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VerificationRunnerTest {

    @Mock
    private VerifierProvider verifierProvider;

    @InjectMocks
    private VerificationRunner verificationRunner;

    @Test
    public void shouldFindAndRunVerifiers() throws Exception {

        final VerificationCommand verificationCommand = mock(VerificationCommand.class);

        final Verifier verifier_1 = mock(Verifier.class);
        final Verifier verifier_2 = mock(Verifier.class);
        final Verifier verifier_3 = mock(Verifier.class);

        final VerificationResult verificationResult_1_a = mock(VerificationResult.class);
        final VerificationResult verificationResult_1_b = mock(VerificationResult.class);
        final VerificationResult verificationResult_2 = mock(VerificationResult.class);
        final VerificationResult verificationResult_3 = mock(VerificationResult.class);

        when(verifierProvider.getVerifiers(verificationCommand)).thenReturn(asList(verifier_1, verifier_2, verifier_3));

        when(verifier_1.verify()).thenReturn(asList(verificationResult_1_a, verificationResult_1_b));
        when(verifier_2.verify()).thenReturn(singletonList(verificationResult_2));
        when(verifier_3.verify()).thenReturn(singletonList(verificationResult_3));

        final List<VerificationResult> verificationResults = verificationRunner.runVerifiers(verificationCommand);

        assertThat(verificationResults.size(), is(4));

        assertThat(verificationResults.get(0), is(verificationResult_1_a));
        assertThat(verificationResults.get(1), is(verificationResult_1_b));
        assertThat(verificationResults.get(2), is(verificationResult_2));
        assertThat(verificationResults.get(3), is(verificationResult_3));
    }
}
