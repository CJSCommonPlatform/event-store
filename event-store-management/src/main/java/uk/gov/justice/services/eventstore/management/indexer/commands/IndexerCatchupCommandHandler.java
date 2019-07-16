package uk.gov.justice.services.eventstore.management.indexer.commands;

import static uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand.INDEXER_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class IndexerCatchupCommandHandler {

    @Inject
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Inject
    private Event<IndexerCatchupRequestedEvent> catchupRequestedEventEventFirer;

    @Inject
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(INDEXER_CATCHUP)
    public void doCatchupWhilstShuttered(final IndexerCatchupCommand catchupCommand) {

        logger.info("Indexer Catchup requested. Shuttering application first");

        final ShutteringRequestedEvent shutteringRequestedEvent = new ShutteringRequestedEvent(
                catchupCommand,
                clock.now());

        shutteringRequestedEventFirer.fire(shutteringRequestedEvent);
    }

    public void onShutteringComplete(@Observes final ShutteringCompleteEvent shutteringCompleteEvent) {

        final SystemCommand systemCommand = shutteringCompleteEvent.getTarget();

        if(systemCommand instanceof IndexerCatchupCommand) {

            logger.info("Received ShutteringComplete event. Now firing IndexerCatchupRequested event");

            final IndexerCatchupRequestedEvent catchupRequestedEvent = new IndexerCatchupRequestedEvent(
                    systemCommand,
                    clock.now());

            catchupRequestedEventEventFirer.fire(catchupRequestedEvent);
        }
    }

    public void onCatchupComplete(@Observes final IndexerCatchupCompletedEvent catchupCompletedEvent) {

        final SystemCommand systemCommand = catchupCompletedEvent.getTarget();
        if(systemCommand instanceof IndexerCatchupCommand) {

            logger.info("Received IndexerCatchupCompleted event. Now firing UnshutteringRequested event");
            final UnshutteringRequestedEvent unshutteringRequestedEvent = new UnshutteringRequestedEvent(
                    systemCommand,
                    clock.now());

            unshutteringRequestedEventFirer.fire(unshutteringRequestedEvent);
        }
    }
}
