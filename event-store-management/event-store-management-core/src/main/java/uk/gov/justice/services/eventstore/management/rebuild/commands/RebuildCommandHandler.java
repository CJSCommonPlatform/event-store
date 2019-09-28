package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.RebuildCommand.REBUILD;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.events.rebuild.RebuildRequestedEvent;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class RebuildCommandHandler {

    @Inject
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(REBUILD)
    public void doRebuild(final RebuildCommand rebuildCommand, final UUID commandId) {

        final ZonedDateTime now = clock.now();
        logger.info(format("Received command '%s' at %tr", rebuildCommand, now));
        rebuildRequestedEventEventFirer.fire(new RebuildRequestedEvent(now, rebuildCommand));
    }
}
