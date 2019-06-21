package uk.gov.justice.services.eventstore.management.indexer.commands;

import uk.gov.justice.services.jmx.command.BaseSystemCommand;

public class IndexerCatchupCommand extends BaseSystemCommand {

    public static final String INDEXER_CATCHUP = "INDEXER_CATCHUP";

    public IndexerCatchupCommand() {
        super(INDEXER_CATCHUP);
    }
}
