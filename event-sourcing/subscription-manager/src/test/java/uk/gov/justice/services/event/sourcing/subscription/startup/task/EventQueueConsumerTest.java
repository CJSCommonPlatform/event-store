package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.FinishedProcessingMessage;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.LinkedList;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventQueueConsumerTest {

    @Mock
    private TransactionalEventProcessor transactionalEventProcessor;

    @Mock
    private EventStreamConsumptionResolver eventStreamConsumptionResolver;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<FinishedProcessingMessage> finishedProcessingMessageArgumentCaptor;

    @Test
    public void shouldConsumeQueueAndCallFinishedListener() {

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);
        final JsonEnvelope event_3 = mock(JsonEnvelope.class);

        final LinkedList<JsonEnvelope> events = new LinkedList<>();
        events.add(event_1);
        events.add(event_2);
        events.add(event_3);

        final EventQueueConsumer eventQueueConsumer = new EventQueueConsumer(
                transactionalEventProcessor,
                eventStreamConsumptionResolver,
                logger);

        when(eventStreamConsumptionResolver.isEventConsumptionComplete(any(FinishedProcessingMessage.class))).thenReturn(true);

        final Boolean result = eventQueueConsumer.consumeEventQueue(events);

        assertThat(result, is(true));

        verify(transactionalEventProcessor).processWithEventBuffer(event_1);
        verify(transactionalEventProcessor).processWithEventBuffer(event_2);
        verify(transactionalEventProcessor).processWithEventBuffer(event_3);
        verify(eventStreamConsumptionResolver).isEventConsumptionComplete(finishedProcessingMessageArgumentCaptor.capture());

        assertThat(finishedProcessingMessageArgumentCaptor.getValue().getQueue(), is(events));
    }

    @Test
    public void shouldLogExceptionAndContinueToProcessEventQueue() {

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);
        final JsonEnvelope event_3 = mock(JsonEnvelope.class);

        final LinkedList<JsonEnvelope> events = new LinkedList<>();
        events.add(event_1);
        events.add(event_2);
        events.add(event_3);

        final Metadata metadata = metadataBuilder()
                .withId(UUID.fromString("c263b05f-d27a-4032-a20f-0665e9f897ca"))
                .withName("event.test")
                .withEventNumber(2)
                .withStreamId(UUID.fromString("93a0a3c5-6937-4f7a-aea8-16983066fac7"))
                .withPosition(2)
                .build();
        final RuntimeException runtimeException = new RuntimeException("Failed");

        final EventQueueConsumer eventQueueConsumer = new EventQueueConsumer(
                transactionalEventProcessor,
                eventStreamConsumptionResolver,
                logger);


        when(transactionalEventProcessor.processWithEventBuffer(event_2)).thenThrow(runtimeException);
        when(event_2.metadata()).thenReturn(metadata);
        when(eventStreamConsumptionResolver.isEventConsumptionComplete(any(FinishedProcessingMessage.class))).thenReturn(true);

        final Boolean result = eventQueueConsumer.consumeEventQueue(events);

        assertThat(result, is(true));

        verify(transactionalEventProcessor).processWithEventBuffer(event_1);
        verify(transactionalEventProcessor).processWithEventBuffer(event_2);
        verify(transactionalEventProcessor).processWithEventBuffer(event_3);
        verify(eventStreamConsumptionResolver).isEventConsumptionComplete(finishedProcessingMessageArgumentCaptor.capture());
        verify(logger).error("Failed to process event with metadata: {\"stream\":{\"id\":\"93a0a3c5-6937-4f7a-aea8-16983066fac7\",\"version\":2},\"name\":\"event.test\",\"id\":\"c263b05f-d27a-4032-a20f-0665e9f897ca\",\"event\":{\"eventNumber\":2}}", runtimeException);

        assertThat(finishedProcessingMessageArgumentCaptor.getValue().getQueue(), is(events));
    }

    @Test
    public void shouldConsumeQueueAndFinishedListenerShouldReturnFalseIfQueueNotFullyConsumed() throws Exception {

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);
        final JsonEnvelope event_3 = mock(JsonEnvelope.class);

        final LinkedList<JsonEnvelope> events = new LinkedList<>();
        events.add(event_1);
        events.add(event_2);
        events.add(event_3);

        final EventQueueConsumer eventQueueConsumer = new EventQueueConsumer(
                transactionalEventProcessor,
                eventStreamConsumptionResolver,
                logger);

        when(eventStreamConsumptionResolver.isEventConsumptionComplete(any(FinishedProcessingMessage.class))).thenReturn(false);

        final Boolean result = eventQueueConsumer.consumeEventQueue(events);

        assertThat(result, is(false));

        verify(transactionalEventProcessor).processWithEventBuffer(event_1);
        verify(transactionalEventProcessor).processWithEventBuffer(event_2);
        verify(transactionalEventProcessor).processWithEventBuffer(event_3);
        verify(eventStreamConsumptionResolver).isEventConsumptionComplete(finishedProcessingMessageArgumentCaptor.capture());

        assertThat(finishedProcessingMessageArgumentCaptor.getValue().getQueue(), is(events));
    }
}
