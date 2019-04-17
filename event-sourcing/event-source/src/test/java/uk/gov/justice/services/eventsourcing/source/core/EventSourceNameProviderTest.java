package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceNameProviderTest {

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @InjectMocks
    private EventSourceNameProvider eventSourceNameProvider;

    @Test
    public void shouldGetTheDefaultEventSourceNameFromTheEventSourceRegistry() throws Exception {

        final String eventSouceName = "Event Souce Name";

        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(eventSourceDefinition.getName()).thenReturn(eventSouceName);

        assertThat(eventSourceNameProvider.getDefaultEventSourceName(), is(eventSouceName));
    }
}
