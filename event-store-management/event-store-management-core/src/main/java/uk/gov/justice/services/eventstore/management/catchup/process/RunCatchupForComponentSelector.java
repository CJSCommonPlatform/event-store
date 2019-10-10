package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import javax.inject.Inject;

public class RunCatchupForComponentSelector {

    @Inject
    private CatchupTypeSelector catchupTypeSelector;

    public boolean shouldRunForThisComponentAndType(final String componentName, final CatchupCommand catchupCommand) {

        final boolean eventCatchup = catchupTypeSelector.isEventCatchup(componentName, catchupCommand);
        final boolean indexerCatchup = catchupTypeSelector.isIndexerCatchup(componentName, catchupCommand);

        return eventCatchup || indexerCatchup;
    }


}
