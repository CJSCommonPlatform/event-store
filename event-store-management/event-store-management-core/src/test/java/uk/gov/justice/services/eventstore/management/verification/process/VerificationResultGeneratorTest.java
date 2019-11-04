package uk.gov.justice.services.eventstore.management.verification.process;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventstore.management.CommandResult;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class VerificationResultGeneratorTest {

    @InjectMocks
    private CommandResultGenerator commandResultGenerator;

    @Test
    public void shouldGenerateSuccessfulResultIfNoErrorsExist() throws Exception {

        final UUID commandId = UUID.randomUUID();

        final List<VerificationResult> successfulResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> warningResults = emptyList();
        final List<VerificationResult> errorResults = emptyList();

        final CommandResult commandResult = commandResultGenerator.createCommandResult(
                commandId,
                successfulResults,
                warningResults,
                errorResults
        );

        assertThat(commandResult.getCommandId(), is(commandId));
        assertThat(commandResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(commandResult.getMessage(), is("Verification of Catchup completed successfully with 0 Error(s), 0 Warning(s) and 1 Success(es)"));
    }

    @Test
    public void shouldStillMarkAsSuccessfulIfWarningsExist() throws Exception {

        final UUID commandId = UUID.randomUUID();

        final List<VerificationResult> successfulResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> warningResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> errorResults = emptyList();

        final CommandResult commandResult = commandResultGenerator.createCommandResult(
                commandId,
                successfulResults,
                warningResults,
                errorResults
        );

        assertThat(commandResult.getCommandId(), is(commandId));
        assertThat(commandResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(commandResult.getMessage(), is("Verification of Catchup completed successfully with 0 Error(s), 1 Warning(s) and 1 Success(es)"));
    }

    @Test
    public void shouldGenerateErrorResultIfAnyErrorsExist() throws Exception {

        final UUID commandId = UUID.randomUUID();

        final List<VerificationResult> successfulResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> warningResults = singletonList(mock(VerificationResult.class));
        final List<VerificationResult> errorResults = singletonList(mock(VerificationResult.class));

        final CommandResult commandResult = commandResultGenerator.createCommandResult(
                commandId,
                successfulResults,
                warningResults,
                errorResults
        );

        assertThat(commandResult.getCommandId(), is(commandId));
        assertThat(commandResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(commandResult.getMessage(), is("Verification of Catchup failed with 1 Error(s), 1 Warning(s) and 1 Success(es)"));
    }
}
