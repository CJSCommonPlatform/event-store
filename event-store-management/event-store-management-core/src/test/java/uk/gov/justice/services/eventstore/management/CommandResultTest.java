package uk.gov.justice.services.eventstore.management;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.eventstore.management.CommandResult.failure;
import static uk.gov.justice.services.eventstore.management.CommandResult.success;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class CommandResultTest {

    @Test
    public void shouldCreateASuccessfulResult() throws Exception {

        final UUID commandId = randomUUID();
        final String message = "message";

        final CommandResult successfulCommandResult = success(
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

        final CommandResult failedCommandResult = failure(
                commandId,
                message
        );

        assertThat(failedCommandResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(failedCommandResult.getCommandId(), is(commandId));
        assertThat(failedCommandResult.getMessage(), is(message));
    }
}
