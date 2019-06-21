package uk.gov.justice.services.eventstore.management.indexer.commands;

import static uk.gov.justice.services.eventstore.management.indexer.commands.IndexerCatchupCommand.INDEXER_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class IndexerCatchupCommandHandler {

    @Inject
    private UtcClock utcClock;

    @Inject
    private Event<IndexerCatchupRequestedEvent> catchupRequestedEventFirer;

    @HandlesSystemCommand(INDEXER_CATCHUP)
    public void doCatchup(final IndexerCatchupCommand catchupIndexerCommand) {
        catchupRequestedEventFirer.fire(new IndexerCatchupRequestedEvent(catchupIndexerCommand, utcClock.now()));
    }
}
