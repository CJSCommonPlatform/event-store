package uk.gov.justice.services.eventstore.management.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class VerifyRebuildCommand extends BaseSystemCommand implements VerificationCommand {

    public static final String VERIFY_REBUILD = "VERIFY_REBUILD";
    private static final String DESCRIPTION = "Runs various verifications on event_log and published_event to check that REBUILD has run successfully";

    public VerifyRebuildCommand() {
        super(VERIFY_REBUILD, DESCRIPTION);
    }
}
