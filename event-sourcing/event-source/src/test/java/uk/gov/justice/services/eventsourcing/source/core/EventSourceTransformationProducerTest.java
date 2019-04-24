package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceTransformationProducerTest {

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @InjectMocks
    private EventSourceTransformationProducer eventSourceTransformationProducer;

    @Test
    public void shouldCreateEventSourceTransformation() throws Exception {

        final String eventSourceName = "eventSourceName";
        
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(eventSourceDefinition.getName()).thenReturn(eventSourceName);

        final EventSourceTransformation eventSourceTransformation = eventSourceTransformationProducer.eventSourceTransformation();

        assertThat(eventSourceTransformation, is(instanceOf(DefaultEventSourceTransformation.class)));

        final DefaultEventSourceTransformation defaultEventSourceTransformation = (DefaultEventSourceTransformation) eventSourceTransformation;

        final EventStreamManager eventStreamManagerField = getValueOfField(defaultEventSourceTransformation, "eventStreamManager", EventStreamManager.class);
        assertThat(eventStreamManagerField, is(eventStreamManager));
    }
}
