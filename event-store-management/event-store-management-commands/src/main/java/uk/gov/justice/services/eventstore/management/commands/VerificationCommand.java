package uk.gov.justice.services.eventstore.management.commands;

import static uk.gov.justice.services.eventstore.management.commands.VerifyCatchupCommand.VERIFY_CATCHUP;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

public interface VerificationCommand extends SystemCommand {

    default boolean isCatchupVerification() {
        return getName().equals(VERIFY_CATCHUP);
    }
}
