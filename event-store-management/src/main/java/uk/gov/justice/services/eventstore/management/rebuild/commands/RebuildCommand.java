package uk.gov.justice.services.eventstore.management.rebuild.commands;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class RebuildCommand extends BaseSystemCommand {

    public static final String REBUILD = "REBUILD";

    public RebuildCommand() {
        super(REBUILD);
    }
}
