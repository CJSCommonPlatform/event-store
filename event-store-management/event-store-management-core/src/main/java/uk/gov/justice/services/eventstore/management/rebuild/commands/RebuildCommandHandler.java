package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static uk.gov.justice.services.jmx.api.command.RebuildCommand.REBUILD;

import uk.gov.justice.services.eventstore.management.rebuild.process.RebuildProcessRunner;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;

import java.util.UUID;

import javax.inject.Inject;

public class RebuildCommandHandler {

    @Inject
    private RebuildProcessRunner rebuildProcessRunner;

    @Inject
    private MdcLogger mdcLogger;


    @HandlesSystemCommand(REBUILD)
    public void doRebuild(final RebuildCommand rebuildCommand, final UUID commandId) {

        mdcLogger.mdcLoggerConsumer().accept(() -> {
            rebuildProcessRunner.runRebuild(commandId, rebuildCommand);
        });
    }
}
