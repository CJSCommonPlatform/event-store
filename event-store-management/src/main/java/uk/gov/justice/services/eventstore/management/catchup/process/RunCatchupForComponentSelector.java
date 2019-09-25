package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType;

import javax.inject.Inject;

public class RunCatchupForComponentSelector {

    @Inject
    private CatchupTypeSelector catchupTypeSelector;

    public boolean shouldRunForThisComponentAndType(final String componentName, final CatchupType catchupType) {

        final boolean eventCatchup = catchupTypeSelector.isEventCatchup(componentName, catchupType);
        final boolean indexerCatchup = catchupTypeSelector.isIndexerCatchup(componentName, catchupType);

        return eventCatchup || indexerCatchup;
    }


}
