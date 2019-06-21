package uk.gov.justice.services.eventstore.management.shuttercatchup.commands;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class ShutterCatchupCommand extends BaseSystemCommand {

    public static final String SHUTTER_CATCHUP = "SHUTTER_CATCHUP";

    public ShutterCatchupCommand() {
        super(SHUTTER_CATCHUP);
    }
}
