package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.EventSourceNameQualifier;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceProviderTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Instance<EventSource> eventSourceInstance;

    @InjectMocks
    private EventSourceProvider eventSourceProvider;

    @Test
    public void shouldGetTheCorrectEventSourceByName() throws Exception {

        final String eventSourceName = "eventSourceName";

        final EventSource eventSource = mock(EventSource.class);

        when(eventSourceInstance.select(new EventSourceNameQualifier(eventSourceName)).get()).thenReturn(eventSource);

        assertThat(eventSourceProvider.getEventSource(eventSourceName), is(eventSource));
    }
}
