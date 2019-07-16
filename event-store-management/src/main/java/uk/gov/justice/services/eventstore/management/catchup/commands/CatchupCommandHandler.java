package uk.gov.justice.services.eventstore.management.catchup.commands;

import static uk.gov.justice.services.jmx.api.command.CatchupCommand.CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupCommandHandler {

    @Inject
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Inject
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Inject
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;


    @HandlesSystemCommand(CATCHUP)
    public void doCatchupWhilstShuttered(final CatchupCommand catchupCommand) {

        logger.info("Catchup requested. Shuttering application first");

        final ShutteringRequestedEvent shutteringRequestedEvent = new ShutteringRequestedEvent(
                catchupCommand,
                clock.now());

        shutteringRequestedEventFirer.fire(shutteringRequestedEvent);
    }

    public void onShutteringComplete(@Observes final ShutteringCompleteEvent shutteringCompleteEvent) {

        final SystemCommand systemCommand = shutteringCompleteEvent.getTarget();

        if(systemCommand instanceof CatchupCommand) {

            logger.info("Received ShutteringComplete event. Now firing CatchupRequested event");

            final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(
                    systemCommand,
                    clock.now());

            catchupRequestedEventFirer.fire(catchupRequestedEvent);
        }
    }

    public void onCatchupComplete(@Observes final CatchupCompletedEvent catchupCompletedEvent) {

        final SystemCommand systemCommand = catchupCompletedEvent.getTarget();
        if(systemCommand instanceof CatchupCommand) {

            logger.info("Received CatchupCompleted event. Now firing UnshutteringRequested event");
            final UnshutteringRequestedEvent unshutteringRequestedEvent = new UnshutteringRequestedEvent(
                    systemCommand,
                    clock.now());

            unshutteringRequestedEventFirer.fire(unshutteringRequestedEvent);
        }
    }
}
