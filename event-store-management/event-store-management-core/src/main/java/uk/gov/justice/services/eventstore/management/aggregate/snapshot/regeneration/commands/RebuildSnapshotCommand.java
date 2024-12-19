package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class RebuildSnapshotCommand extends BaseSystemCommand {

    public static final String REBUILD_SNAPSHOTS = "REBUILD_SNAPSHOTS";
    private static final String COMMAND_RUNTIME_ID_TYPE = "streamId";
    private static final String COMMAND_RUNTIME_STRING_TYPE = "aggregate class name";
    private static final String DESCRIPTION = "Forces the generation of a new aggregate snapshot for a given " + COMMAND_RUNTIME_ID_TYPE + " and " + COMMAND_RUNTIME_STRING_TYPE;

    public RebuildSnapshotCommand() {
        super(REBUILD_SNAPSHOTS, DESCRIPTION);
    }

    public boolean requiresCommandRuntimeString() {
        return true;
    }

    @Override
    public String commandRuntimeIdType() {
        return COMMAND_RUNTIME_ID_TYPE;
    }

    @Override
    public String commandRuntimeStringType() {
        return COMMAND_RUNTIME_STRING_TYPE;
    }

    public boolean requiresCommandRuntimeId() {
        return true;
    }
}
