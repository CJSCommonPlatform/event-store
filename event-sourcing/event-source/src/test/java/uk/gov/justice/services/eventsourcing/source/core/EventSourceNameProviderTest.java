package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
