package uk.gov.justice.services.eventstore.management.indexer.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand.INDEXER_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.IndexerCatchupCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class IndexerCatchupCommandHandler {

    @Inject
    private Event<IndexerCatchupRequestedEvent> catchupRequestedEventEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(INDEXER_CATCHUP)
    public void catchupSearchIndexes(final IndexerCatchupCommand catchupCommand) {

        final ZonedDateTime now = clock.now();
        logger.info(format("Received command '%s' at %tr", catchupCommand, now));
        final IndexerCatchupRequestedEvent catchupRequestedEvent = new IndexerCatchupRequestedEvent(
                catchupCommand,
                now);

        catchupRequestedEventEventFirer.fire(catchupRequestedEvent);
    }
}
