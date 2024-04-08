package uk.gov.justice.services.eventstore.management.replay.process;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@RunWith(MockitoJUnitRunner.class)
public class ReplayEventToEventListenerProcessorBeanTest {

    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID COMMAND_RUNTIME_ID = UUID.randomUUID();
    private static final String EVENT_SOURCE_NAME = "listenerEventSourceName";

    private static final ReplayEventContext REPLAY_EVENT_CONTEXT = new ReplayEventContext(COMMAND_ID, COMMAND_RUNTIME_ID, EVENT_SOURCE_NAME, EVENT_LISTENER);

    @Mock
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Mock
    private EventBufferProcessorFactory eventBufferProcessorFactory;

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private ReplayEventToEventListenerProcessorBean replayEventToEventListenerProcessorBean;

    @Test
    public void shouldFetchPublishedEventAndInvokeEventBufferProcessor() {
        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);
        final PublishedEvent publishedEvent =  mock(PublishedEvent.class);
        final JsonEnvelope eventEnvelope = mock(JsonEnvelope.class);
        final EventBufferProcessor eventBufferProcessor = mock(EventBufferProcessor.class);
        when(publishedEventSourceProvider.getPublishedEventSource(EVENT_SOURCE_NAME)).thenReturn(publishedEventSource);
        when(publishedEventSource.findByEventId(COMMAND_RUNTIME_ID)).thenReturn(Optional.of(publishedEvent));
        when(eventConverter.envelopeOf(publishedEvent)).thenReturn(eventEnvelope);
        when(eventBufferProcessorFactory.create(EVENT_LISTENER)).thenReturn(eventBufferProcessor);

        replayEventToEventListenerProcessorBean.perform(REPLAY_EVENT_CONTEXT);

        verify(eventBufferProcessor).processWithEventBuffer(eventEnvelope);
    }

    @Test
    public void shouldThrowExceptionWhenPublishedEventFetchFails() {
        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);
        when(publishedEventSourceProvider.getPublishedEventSource(EVENT_SOURCE_NAME)).thenReturn(publishedEventSource);
        when(publishedEventSource.findByEventId(COMMAND_RUNTIME_ID)).thenReturn(Optional.empty());

        final IllegalStateException e = assertThrows(IllegalStateException.class, () -> replayEventToEventListenerProcessorBean.perform(REPLAY_EVENT_CONTEXT));

        assertThat(e.getMessage(), is("Published event not found for given commandRuntimeId:" + COMMAND_RUNTIME_ID + " under event source name:" + EVENT_SOURCE_NAME));
    }
}