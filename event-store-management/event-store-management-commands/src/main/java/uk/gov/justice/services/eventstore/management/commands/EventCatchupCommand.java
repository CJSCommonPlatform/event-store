package uk.gov.justice.services.eventstore.management.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class EventCatchupCommand extends BaseSystemCommand implements CatchupCommand {

    public static final String CATCHUP = "CATCHUP";
    private static final String DESCRIPTION = "Catches up and publishes all Events missing from the View Store";

    public EventCatchupCommand() {
        super(CATCHUP, DESCRIPTION);
    }
}
