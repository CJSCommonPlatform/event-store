package uk.gov.justice.services.eventstore.management.catchup.commands;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class CatchupCommand extends BaseSystemCommand {

    public static final String CATCHUP = "CATCHUP";

    public CatchupCommand() {
        super(CATCHUP);
    }
}
