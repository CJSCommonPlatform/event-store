package uk.gov.justice.services.eventsourcing.util.messaging;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceNameCalculatorTest {

    @InjectMocks
    private EventSourceNameCalculator eventSourceNameCalculator;

    @Test
    public void shouldUseTheSourceNameFromTheEnvelopeIfItExists() throws Exception {

        final String source = "source-context";

        final JsonEnvelope event = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(event.metadata()).thenReturn(metadata);
        when(metadata.source()).thenReturn(Optional.of(source));

        assertThat(eventSourceNameCalculator.getSource(event), is(source));
    }

    @Test
    public void shouldParseTheEventNameToGetTheSourceContext() throws Exception {

        final String eventName = "source-context.event.name";

        final JsonEnvelope event = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(event.metadata()).thenReturn(metadata);
        when(metadata.source()).thenReturn(empty());
        when(metadata.name()).thenReturn(eventName);

        assertThat(eventSourceNameCalculator.getSource(event), is("source-context"));
    }
}
