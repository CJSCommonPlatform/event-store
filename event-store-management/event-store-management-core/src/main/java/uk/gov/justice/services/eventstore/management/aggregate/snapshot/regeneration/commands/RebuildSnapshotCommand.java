package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class RebuildSnapshotCommand extends BaseSystemCommand {

    public static final String REBUILD_SNAPSHOTS = "REBUILD_SNAPSHOTS";
    private static final String DESCRIPTION = "Forces the generation of a new aggregate snapshot for a given stream id";

    public RebuildSnapshotCommand() {
        super(REBUILD_SNAPSHOTS, DESCRIPTION);
    }

    public boolean requiresCommandRuntimeString() {
        return true;
    }

    public boolean requiresCommandRuntimeId() {
        return true;
    }
}
