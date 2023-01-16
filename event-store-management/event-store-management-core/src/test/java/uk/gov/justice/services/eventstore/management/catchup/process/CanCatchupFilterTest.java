package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CanCatchupFilterTest {

    @Mock
    private CatchupTypeSelector catchupTypeSelector;

    @InjectMocks
    private CanCatchupFilter canCatchupFilter;

    @Test
    public void shouldRunIfRunningEventCatchupAndTheComponentIsEventListener() throws Exception {

        final String componentName = "EVENT_LISTENER";
        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();

        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(catchupTypeSelector.isEventCatchup(componentName, eventCatchupCommand)).thenReturn(true);
        when(catchupTypeSelector.isIndexerCatchup(componentName, eventCatchupCommand)).thenReturn(false);

        final boolean shouldRun = canCatchupFilter.canCatchup(subscriptionsDescriptor, eventCatchupCommand);

        assertThat(shouldRun, is(true));
    }

    @Test
    public void shouldRunIfRunningIndexCatchupAndTheComponentIsEventIndexer() throws Exception {

        final String componentName = "EVENT_INDEXER";
        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();
        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(catchupTypeSelector.isEventCatchup(componentName, eventCatchupCommand)).thenReturn(true);
        when(catchupTypeSelector.isIndexerCatchup(componentName, eventCatchupCommand)).thenReturn(false);

        final boolean shouldRun = canCatchupFilter.canCatchup(subscriptionsDescriptor, eventCatchupCommand);

        assertThat(shouldRun, is(true));
    }

    @Test
    public void shouldNotRunIfRunningIfNeitherComponentShouldRun() throws Exception {

        final String componentName = "EVENT_PROCESSOR";
        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();
        final SubscriptionsDescriptor subscriptionsDescriptor = mock(SubscriptionsDescriptor.class);

        when(subscriptionsDescriptor.getServiceComponent()).thenReturn(componentName);
        when(catchupTypeSelector.isEventCatchup(componentName, eventCatchupCommand)).thenReturn(false);
        when(catchupTypeSelector.isIndexerCatchup(componentName, eventCatchupCommand)).thenReturn(false);

        final boolean shouldRun = canCatchupFilter.canCatchup(subscriptionsDescriptor, eventCatchupCommand);

        assertThat(shouldRun, is(false));
    }
}
