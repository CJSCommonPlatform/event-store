package uk.gov.justice.services.eventstore.management.verification.process;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.CommandResult;
import uk.gov.justice.services.eventstore.management.commands.VerificationCommand;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupVerificationProcessTest {

    @Mock
    private VerificationRunner verificationRunner;

    @Mock
    private VerificationResultFilter verificationResultFilter;

    @Mock
    private VerificationResultsLogger verificationResultsLogger;

    @Mock
    private CommandResultGenerator commandResultGenerator;

    @InjectMocks
    private CatchupVerificationProcess catchupVerificationProcess;

    @Test
    public void shouldRunTheVariousVerificationProcessesAndLogTheResults() throws Exception {

        final UUID commandId = randomUUID();
        final VerificationCommand verificationCommand = mock(VerificationCommand.class);

        final List<VerificationResult> verificationResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> successResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> warningResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> errorResults = singletonList(mock(VerificationResult.class));

        final CommandResult commandResult = mock(CommandResult.class);

        when(verificationRunner.runVerifiers(verificationCommand)).thenReturn(verificationResults);
        when(verificationResultFilter.findSuccesses(verificationResults)).thenReturn(successResults);
        when(verificationResultFilter.findWarnings(verificationResults)).thenReturn(warningResults);
        when(verificationResultFilter.findErrors(verificationResults)).thenReturn(errorResults);

        when(commandResultGenerator.createCommandResult(
                commandId,
                verificationCommand,
                successResults,
                warningResults,
                errorResults)).thenReturn(commandResult);

        assertThat(catchupVerificationProcess.runVerification(commandId, verificationCommand), is(commandResult));

        verify(verificationResultsLogger).logResults(
                successResults,
                warningResults,
                errorResults);
    }
}
