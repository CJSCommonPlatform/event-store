package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static uk.gov.justice.services.eventstore.management.rebuild.commands.RebuildCommand.REBUILD;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildCompleteEvent;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildRequestedEvent;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class RebuildCommandHandler {

    @Inject
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Inject
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @Inject
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(REBUILD)
    public void doRebuild(final RebuildCommand rebuildCommand) {
        shutteringRequestedEventFirer.fire(new ShutteringRequestedEvent(rebuildCommand, clock.now()));
    }

    public void onShutteringComplete(@Observes final ShutteringCompleteEvent shutteringCompleteEvent) {

        final SystemCommand systemCommand = shutteringCompleteEvent.getTarget();

        if(systemCommand instanceof RebuildCommand) {

            logger.info("Received ShutteringComplete event. Now firing RebuildRequestedEvent");

            rebuildRequestedEventEventFirer.fire(new RebuildRequestedEvent(clock.now(), systemCommand));
        }
    }

    public void onRebuildComplete(@Observes final RebuildCompleteEvent rebuildCompleteEvent) {

        final SystemCommand systemCommand = rebuildCompleteEvent.getTarget();
        if(systemCommand instanceof RebuildCommand) {

            logger.info("Received RebuildComplete event. Now firing UnshutteringRequested event");

            unshutteringRequestedEventFirer.fire(new UnshutteringRequestedEvent(
                    systemCommand,
                    clock.now()));
        }
    }
}
