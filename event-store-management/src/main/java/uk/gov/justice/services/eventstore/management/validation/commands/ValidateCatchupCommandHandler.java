package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.ValidateCatchupCommand.VALIDATE_CATCHUP;

import uk.gov.justice.services.jmx.api.command.ValidateCatchupCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ValidateCatchupCommandHandler {

    @Inject
    private Logger logger;

    @HandlesSystemCommand(VALIDATE_CATCHUP)
    public void validateCatchup(final ValidateCatchupCommand validateCatchupCommand) {
        logger.warn(format("Command %s not yet implemented", validateCatchupCommand.getName()));
    }
}
