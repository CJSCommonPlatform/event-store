package uk.gov.justice.services.eventstore.management.verification.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.SUCCESS;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.WARNING;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VerificationResultFilterTest {

    @InjectMocks
    private VerificationResultFilter verificationResultFilter;

    @Test
    public void shouldFindAllSuccessfulResults() throws Exception {

        final VerificationResult successfulResult_1 = mock(VerificationResult.class);
        final VerificationResult successfulResult_2 = mock(VerificationResult.class);
        final VerificationResult warningResult_1 = mock(VerificationResult.class);
        final VerificationResult warningResult_2 = mock(VerificationResult.class);
        final VerificationResult errorResult_1 = mock(VerificationResult.class);
        final VerificationResult errorResult_2 = mock(VerificationResult.class);

        final List<VerificationResult> verificationResults = asList(
                successfulResult_1,
                successfulResult_2,
                warningResult_1,
                warningResult_2,
                errorResult_1,
                errorResult_2
        );

        when(successfulResult_1.getVerificationResultType()).thenReturn(SUCCESS);
        when(successfulResult_2.getVerificationResultType()).thenReturn(SUCCESS);
        when(warningResult_1.getVerificationResultType()).thenReturn(WARNING);
        when(warningResult_2.getVerificationResultType()).thenReturn(WARNING);
        when(errorResult_1.getVerificationResultType()).thenReturn(ERROR);
        when(errorResult_2.getVerificationResultType()).thenReturn(ERROR);

        final List<VerificationResult> successfulResults = verificationResultFilter.findSuccesses(verificationResults);

        assertThat(successfulResults.size(), is(2));

        assertThat(successfulResults, hasItem(successfulResult_1));
        assertThat(successfulResults, hasItem(successfulResult_2));
    }

    @Test
    public void shouldFindAllWarningResults() throws Exception {

        final VerificationResult successfulResult_1 = mock(VerificationResult.class);
        final VerificationResult successfulResult_2 = mock(VerificationResult.class);
        final VerificationResult warningResult_1 = mock(VerificationResult.class);
        final VerificationResult warningResult_2 = mock(VerificationResult.class);
        final VerificationResult errorResult_1 = mock(VerificationResult.class);
        final VerificationResult errorResult_2 = mock(VerificationResult.class);

        final List<VerificationResult> verificationResults = asList(
                successfulResult_1,
                successfulResult_2,
                warningResult_1,
                warningResult_2,
                errorResult_1,
                errorResult_2
        );

        when(successfulResult_1.getVerificationResultType()).thenReturn(SUCCESS);
        when(successfulResult_2.getVerificationResultType()).thenReturn(SUCCESS);
        when(warningResult_1.getVerificationResultType()).thenReturn(WARNING);
        when(warningResult_2.getVerificationResultType()).thenReturn(WARNING);
        when(errorResult_1.getVerificationResultType()).thenReturn(ERROR);
        when(errorResult_2.getVerificationResultType()).thenReturn(ERROR);

        final List<VerificationResult> warningResults = verificationResultFilter.findWarnings(verificationResults);

        assertThat(warningResults.size(), is(2));

        assertThat(warningResults, hasItem(warningResult_1));
        assertThat(warningResults, hasItem(warningResult_2));
    }

    @Test
    public void shouldFindAllErrorResults() throws Exception {

        final VerificationResult successfulResult_1 = mock(VerificationResult.class);
        final VerificationResult successfulResult_2 = mock(VerificationResult.class);
        final VerificationResult warningResult_1 = mock(VerificationResult.class);
        final VerificationResult warningResult_2 = mock(VerificationResult.class);
        final VerificationResult errorResult_1 = mock(VerificationResult.class);
        final VerificationResult errorResult_2 = mock(VerificationResult.class);

        final List<VerificationResult> verificationResults = asList(
                successfulResult_1,
                successfulResult_2,
                warningResult_1,
                warningResult_2,
                errorResult_1,
                errorResult_2
        );

        when(successfulResult_1.getVerificationResultType()).thenReturn(SUCCESS);
        when(successfulResult_2.getVerificationResultType()).thenReturn(SUCCESS);
        when(warningResult_1.getVerificationResultType()).thenReturn(WARNING);
        when(warningResult_2.getVerificationResultType()).thenReturn(WARNING);
        when(errorResult_1.getVerificationResultType()).thenReturn(ERROR);
        when(errorResult_2.getVerificationResultType()).thenReturn(ERROR);

        final List<VerificationResult> errorResults = verificationResultFilter.findErrors(verificationResults);

        assertThat(errorResults.size(), is(2));

        assertThat(errorResults, hasItem(errorResult_1));
        assertThat(errorResults, hasItem(errorResult_2));
    }
}
