package uk.gov.justice.services.eventstore.management.validation.commands;

import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.jmx.api.domain.CommandState;

import java.util.Objects;
import java.util.UUID;

public class VerificationCommandResult {

    private final UUID commandId;
    private final String message;
    private final CommandState commandState;

    private VerificationCommandResult(final UUID commandId, final String message, final CommandState commandState) {
        this.commandId = commandId;
        this.message = message;
        this.commandState = commandState;
    }

    public static VerificationCommandResult success(final UUID commandId, final String message) {
        return new VerificationCommandResult(
                commandId,
                message,
                COMMAND_COMPLETE
        );
    }

    public static VerificationCommandResult failure(final UUID commandId, final String message) {
        return new VerificationCommandResult(
                commandId,
                message,
                COMMAND_FAILED
        );
    }

    public UUID getCommandId() {
        return commandId;
    }

    public String getMessage() {
        return message;
    }

    public CommandState getCommandState() {
        return commandState;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationCommandResult)) return false;
        final VerificationCommandResult that = (VerificationCommandResult) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(message, that.message) &&
                commandState == that.commandState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, message, commandState);
    }

    @Override
    public String toString() {
        return "VerificationResult{" +
                "commandId=" + commandId +
                ", message='" + message + '\'' +
                ", commandState=" + commandState +
                '}';
    }
}
