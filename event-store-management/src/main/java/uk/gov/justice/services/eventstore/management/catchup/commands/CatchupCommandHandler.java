package uk.gov.justice.services.eventstore.management.catchup.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType.INDEX_CATCHUP;
import static uk.gov.justice.services.jmx.api.command.CatchupCommand.CATCHUP;
import static uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand.INDEXER_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupCommandHandler {

    @Inject
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(CATCHUP)
    public void catchupEvents(final CatchupCommand catchupCommand) {
        doCatchup(catchupCommand, EVENT_CATCHUP);
    }

    @HandlesSystemCommand(INDEXER_CATCHUP)
    public void catchupSearchIndexes(final IndexerCatchupCommand indexerCatchupCommand) {
        doCatchup(indexerCatchupCommand, INDEX_CATCHUP);
    }

    private void doCatchup(final SystemCommand systemCommand, final CatchupType catchupType) {
        final ZonedDateTime now = clock.now();

        logger.info(format("Received command '%s' at %tr", systemCommand, now));
        final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(
                catchupType,
                systemCommand,
                now);

        catchupRequestedEventFirer.fire(catchupRequestedEvent);
    }
}
