package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;

import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import javax.enterprise.inject.CreationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceProducerTest {

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Mock
    private JdbcEventSourceFactory jdbcEventSourceFactory;

    @InjectMocks
    private EventSourceProducer eventSourceProducer;

    @Test
    public void shouldCreateDefaultEventSourceDefinitionWhenEventSourceNameIsEmpty() throws Exception {

        final String jndiDataSourceName = "jndiDataSourceName";
        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefault(true)
                .withLocation(new Location("", empty(), of(jndiDataSourceName)))
                .build();
        final EventSourceName eventSourceName = mock(EventSourceName.class);
        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(jdbcEventSourceFactory.create(eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldCreateDefaultEventSourceWhenNoEventSourceNameQualifierSet() throws Exception {

        final String jndiDataSourceName = "jndiDataSourceName";
        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefault(true)
                .withLocation(new Location("", empty(), of(jndiDataSourceName)))
                .build();

        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(jdbcEventSourceFactory.create(eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldFailIfNoDataSourceNameFoundInEventSourcesYaml() throws Exception {

        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefault(true)
                .withLocation(new Location("", empty(), empty()))
                .build();
        final EventSourceName eventSourceName = mock(EventSourceName.class);
        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);

        try {
            eventSourceProducer.eventSource();
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("No DataSource specified for EventSource 'defaultEventSource' specified in event-sources.yaml"));
        }

        verifyNoInteractions(jdbcEventSourceFactory);
    }
}


