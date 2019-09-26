package uk.gov.justice.services.eventstore.management.catchup.process;

import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

public class CatchupTypeSelector {

    public boolean isEventCatchup(final String componentName, final CatchupType catchupType) {
        return componentName.contains(EVENT_LISTENER) && catchupType == EVENT_CATCHUP;
    }

    public boolean isIndexerCatchup(final String componentName, final CatchupType catchupType) {
        return componentName.contains(EVENT_INDEXER) && catchupType == INDEX_CATCHUP;
    }
}
