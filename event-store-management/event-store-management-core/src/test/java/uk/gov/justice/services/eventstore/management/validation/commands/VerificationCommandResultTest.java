package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult.failure;
import static uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult.success;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import java.util.UUID;

import org.junit.Test;

public class VerificationCommandResultTest {

    @Test
    public void shouldCreateASuccessfulResult() throws Exception {

        final UUID commandId = randomUUID();
        final String message = "message";

        final VerificationCommandResult successfulCommandResult = success(
                commandId,
                message
        );

        assertThat(successfulCommandResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(successfulCommandResult.getCommandId(), is(commandId));
        assertThat(successfulCommandResult.getMessage(), is(message));
    }

    @Test
    public void shouldCreateAFailureResult() throws Exception {

        final UUID commandId = randomUUID();
        final String message = "message";

        final VerificationCommandResult failedCommandResult = failure(
                commandId,
                message
        );

        assertThat(failedCommandResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(failedCommandResult.getCommandId(), is(commandId));
        assertThat(failedCommandResult.getMessage(), is(message));
    }
}
