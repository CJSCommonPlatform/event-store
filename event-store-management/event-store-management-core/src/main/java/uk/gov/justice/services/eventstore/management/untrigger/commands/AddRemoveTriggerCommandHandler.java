package uk.gov.justice.services.eventstore.management.untrigger.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.AddTriggerCommand.ADD_TRIGGER;
import static uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand.REMOVE_TRIGGER;

import uk.gov.justice.services.eventstore.management.untrigger.process.AddRemoveTriggerProcessRunner;
import uk.gov.justice.services.jmx.api.command.AddTriggerCommand;
import uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class AddRemoveTriggerCommandHandler {

    @Inject
    private AddRemoveTriggerProcessRunner addRemoveTriggerProcessRunner;

    @Inject
    private MdcLogger mdcLogger;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(ADD_TRIGGER)
    public void addTriggerToEventLogTable(final AddTriggerCommand addTriggerCommand, final UUID commandId) {

        mdcLogger.mdcLoggerConsumer().accept(() -> {

            logger.info(format("Received command %s", addTriggerCommand.getName()));

            addRemoveTriggerProcessRunner.addTriggerToEventLogTable(commandId, addTriggerCommand);
        });
    }

    @HandlesSystemCommand(REMOVE_TRIGGER)
    public void removeTriggerFromEventLogTable(final RemoveTriggerCommand removeTriggerCommand, final UUID commandId) {

        mdcLogger.mdcLoggerConsumer().accept(() -> {

            logger.info(format("Received command %s", removeTriggerCommand.getName()));

            addRemoveTriggerProcessRunner.removeTriggerFromEventLogTable(commandId, removeTriggerCommand);
        });
    }
}
