package uk.gov.justice.services.event.sourcing.subscription.startup.task;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.FinishedProcessingMessage;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.json.Json;

@RunWith(MockitoJUnitRunner.class)
public class EventQueueConsumerTest {

    @Mock
    private TransactionalEventProcessor transactionalEventProcessor;

    @Mock
    private EventStreamConsumptionResolver eventStreamConsumptionResolver;

    @Mock
    private Logger logger;


    @InjectMocks
    private EventQueueConsumer eventQueueConsumer;

    @Test
    public void shouldProcessAllEventsOnQueueAndReturnTrueIfComplete() throws Exception {

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);

        final Queue<JsonEnvelope> eventQueue = new ConcurrentLinkedQueue<>();

        when(eventStreamConsumptionResolver.isEventConsumptionComplete(new FinishedProcessingMessage(eventQueue))).thenReturn(true);

        eventQueue.add(event_1);
        eventQueue.add(event_2);
        final String subscriptionName = "subscriptionName";

        eventQueueConsumer.consumeEventQueue(eventQueue, subscriptionName);

        final InOrder inOrder = inOrder(transactionalEventProcessor);

        inOrder.verify(transactionalEventProcessor).processWithEventBuffer(event_1, subscriptionName);
        inOrder.verify(transactionalEventProcessor).processWithEventBuffer(event_2, subscriptionName);
    }

    @Test
    public void shouldLogAnyExceptionsThrownWhilstProcessing() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final UUID eventId = fromString("1b352632-b62c-49d4-a5fd-546ce9cbd2f1");
        final JsonEnvelope event_2 = envelopeFrom(
                metadataBuilder().withId(eventId).withName("an-event"),
                createObjectBuilder());

        final Queue<JsonEnvelope> eventQueue = new ConcurrentLinkedQueue<>();

        when(eventStreamConsumptionResolver.isEventConsumptionComplete(new FinishedProcessingMessage(eventQueue))).thenReturn(true);

        eventQueue.add(event_1);
        eventQueue.add(event_2);
        final String subscriptionName = "subscriptionName";

        doThrow(nullPointerException).when(transactionalEventProcessor).processWithEventBuffer(event_2, subscriptionName);

        eventQueueConsumer.consumeEventQueue(eventQueue, subscriptionName);

        verify(transactionalEventProcessor).processWithEventBuffer(event_1, subscriptionName);

        verify(logger).error("Failed to process event with metadata: {\"name\":\"an-event\",\"id\":\"1b352632-b62c-49d4-a5fd-546ce9cbd2f1\"}", nullPointerException);
    }
}
