package uk.gov.justice.services.eventstore.management.catchup.commands;

import static uk.gov.justice.services.eventstore.management.catchup.commands.CatchupCommand.CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class CatchupCommandHandler {

    @Inject
    private UtcClock utcClock;

    @Inject
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @HandlesSystemCommand(CATCHUP)
    public void doCatchup() {
        catchupRequestedEventFirer.fire(new CatchupRequestedEvent(new CatchupCommand(), utcClock.now()));
    }
}
