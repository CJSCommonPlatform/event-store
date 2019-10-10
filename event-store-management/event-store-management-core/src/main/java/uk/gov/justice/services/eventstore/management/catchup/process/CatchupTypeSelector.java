package uk.gov.justice.services.eventstore.management.catchup.process;

import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;

public class CatchupTypeSelector {

    public boolean isEventCatchup(final String componentName, final CatchupCommand catchupCommand) {
        return componentName.contains(EVENT_LISTENER) && catchupCommand.isEventCatchup();
    }

    public boolean isIndexerCatchup(final String componentName, final CatchupCommand catchupCommand) {
        return componentName.contains(EVENT_INDEXER) && (! catchupCommand.isEventCatchup());
    }
}
