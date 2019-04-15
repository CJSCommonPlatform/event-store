package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;

import uk.gov.justice.services.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import java.util.Optional;

import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceProducerTest {

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Mock
    private JdbcEventSourceFactory jdbcEventSourceFactory;

    @Mock
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private EventSourceProducer eventSourceProducer;


    @Test
    public void shouldCreateDefaultEventSourceDefinitionWhenEventSourceNameIsEmpty() throws Exception {
        final String jndiDataSourceName = "jndiDataSourceName";
        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefault(true)
                .withLocation(new Location("", "", Optional.of(jndiDataSourceName)))
                .build();
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);

        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);
        final DataSource dataSource = mock(DataSource.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn("");
        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(jdbcDataSourceProvider.getDataSource(jndiDataSourceName)).thenReturn(dataSource);
        when(jdbcEventSourceFactory.create(dataSource, eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldCreateDefaultEventSourceWhenNoEventSourceNameQualifierSet() throws Exception {

        final String jndiDataSourceName = "jndiDataSourceName";
        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefault(true)
                .withLocation(new Location("", "", Optional.of(jndiDataSourceName)))
                .build();

        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(jdbcDataSourceProvider.getDataSource(jndiDataSourceName)).thenReturn(dataSource);
        when(jdbcEventSourceFactory.create(dataSource, eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldCreateAnEventSourceUsingTheEventSourceNameAnnotation() throws Exception {

        final String eventSourceName = "eventSourceName";
        final String jndiDataSourceName = "jndiDataSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);
        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);
        final DataSource dataSource = mock(DataSource.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn(eventSourceName);
        when(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName)).thenReturn(Optional.of(eventSourceDefinition));
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(of(jndiDataSourceName));
        when(jdbcDataSourceProvider.getDataSource(jndiDataSourceName)).thenReturn(dataSource);
        when(jdbcEventSourceFactory.create(dataSource, eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(injectionPoint), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldFailIfNoEventSourceFoundInTheEventSourceRegistry() throws Exception {

        final String eventSourceName = "my-event-source";
        final String jndiDataSourceName = "jndiDataSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);
        final DataSource dataSource = mock(DataSource.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn(eventSourceName);
        when(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName)).thenReturn(empty());
        when(jdbcDataSourceProvider.getDataSource(jndiDataSourceName)).thenReturn(dataSource);

        try {
            eventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("Failed to find EventSource named 'my-event-source' in event-sources.yaml"));
        }

        verifyZeroInteractions(jdbcEventSourceFactory);
    }

    @Test
    public void shouldFailIfNoDataSourceNameFoundInEventSourcesYaml() throws Exception {

        final String eventSourceName = "eventSourceName";
        final String jndiDataSourceName = "jndiDataSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);
        final DataSource dataSource = mock(DataSource.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn(eventSourceName);
        when(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName)).thenReturn(Optional.of(eventSourceDefinition));
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(empty());
        when(eventSourceDefinition.getName()).thenReturn(jndiDataSourceName);

        try {
            eventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("No DataSource specified for EventSource 'jndiDataSourceName' specified in event-sources.yaml"));
        }

        verifyZeroInteractions(jdbcEventSourceFactory);
    }
}


