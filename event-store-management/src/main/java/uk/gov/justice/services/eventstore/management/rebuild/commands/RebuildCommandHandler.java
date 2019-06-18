package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static uk.gov.justice.services.eventstore.management.rebuild.commands.RebuildCommand.REBUILD;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildRequestedEvent;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class RebuildCommandHandler {

    @Inject
    private UtcClock clock;

    @Inject
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @HandlesSystemCommand(REBUILD)
    public void doRebuild(final RebuildCommand rebuildCommand) {
        rebuildRequestedEventEventFirer.fire(new RebuildRequestedEvent(clock.now(), rebuildCommand));
    }
}
