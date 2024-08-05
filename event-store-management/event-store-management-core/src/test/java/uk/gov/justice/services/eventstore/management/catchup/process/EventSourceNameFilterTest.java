package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceNameFilterTest {

    @Mock
    private CatchupWhitelistedEventSourcesProvider catchupWhitelistedEventSourcesProvider;

    @InjectMocks
    private EventSourceNameFilter eventSourceNameFilter;

    @Test
    public void shouldReturnTrueIfSubscriptionEventSourceNameIsInOurListOfWhitelistedEventSources() throws Exception {

        final String eventSource_1 = "eventSource_1";
        final String eventSource_2 = "eventSource_2";

        final Subscription subscription = mock(Subscription.class);

        when(catchupWhitelistedEventSourcesProvider.getWhiteListedEventSources()).thenReturn(of(asList(eventSource_1, eventSource_2)));

        when(subscription.getEventSourceName()).thenReturn(eventSource_1, eventSource_2, "something-else");

        assertThat(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription), is(true));
        assertThat(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription), is(true));
        assertThat(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription), is(false));
    }

    @Test
    public void shouldReturnTrueByDefaultIfOurListOfWhitelistedEventSourcesIsEmpty() throws Exception {

        final Subscription subscription = mock(Subscription.class);

        when(catchupWhitelistedEventSourcesProvider.getWhiteListedEventSources()).thenReturn(empty());

        assertThat(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription), is(true));
        assertThat(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription), is(true));
        assertThat(eventSourceNameFilter.shouldRunCatchupAgainstEventSource(subscription), is(true));
    }
}