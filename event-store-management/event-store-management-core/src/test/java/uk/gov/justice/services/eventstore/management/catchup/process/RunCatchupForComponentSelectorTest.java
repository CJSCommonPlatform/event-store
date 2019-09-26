package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunCatchupForComponentSelectorTest {

    @Mock
    private CatchupTypeSelector catchupTypeSelector;

    @InjectMocks
    private RunCatchupForComponentSelector runCatchupForComponentSelector;

    @Test
    public void shouldRunIfRunningEventCatchupAndTheComponentIsEventListener() throws Exception {

        final String componentName = "EVENT_LISTENER";
        final CatchupType catchupType = EVENT_CATCHUP;

        when(catchupTypeSelector.isEventCatchup(componentName, catchupType)).thenReturn(true);
        when(catchupTypeSelector.isIndexerCatchup(componentName, catchupType)).thenReturn(false);

        final boolean shouldRun = runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, catchupType);

        assertThat(shouldRun, is(true));
    }

    @Test
    public void shouldRunIfRunningIndexCatchupAndTheComponentIsEventIndexer() throws Exception {

        final String componentName = "EVENT_INDEXER";
        final CatchupType catchupType = INDEX_CATCHUP;

        when(catchupTypeSelector.isEventCatchup(componentName, catchupType)).thenReturn(true);
        when(catchupTypeSelector.isIndexerCatchup(componentName, catchupType)).thenReturn(false);

        final boolean shouldRun = runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, catchupType);

        assertThat(shouldRun, is(true));
    }

    @Test
    public void shouldNotRunIfRunningIfNeitherComponentShouldRun() throws Exception {

        final String componentName = "EVENT_PROCESSOR";
        final CatchupType catchupType = INDEX_CATCHUP;

        when(catchupTypeSelector.isEventCatchup(componentName, catchupType)).thenReturn(false);
        when(catchupTypeSelector.isIndexerCatchup(componentName, catchupType)).thenReturn(false);

        final boolean shouldRun = runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, catchupType);

        assertThat(shouldRun, is(false));
    }
}
