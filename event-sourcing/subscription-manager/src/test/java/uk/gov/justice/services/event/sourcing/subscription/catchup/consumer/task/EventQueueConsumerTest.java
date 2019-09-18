package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.FinishedProcessingMessage;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
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


    @InjectMocks
    private EventQueueConsumer eventQueueConsumer;

    @Test
    public void shouldProcessAllEventsOnQueueAndReturnTrueIfComplete() throws Exception {

        final PublishedEvent event_1 = mock(PublishedEvent.class);
        final PublishedEvent event_2 = mock(PublishedEvent.class);

        final Queue<PublishedEvent> eventQueue = new ConcurrentLinkedQueue<>();

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

        final PublishedEvent event_1 = mock(PublishedEvent.class);
        final String metadata = "{some: metadata}";
        final PublishedEvent event_2 = mock(PublishedEvent.class);

        final Queue<PublishedEvent> eventQueue = new ConcurrentLinkedQueue<>();

        when(eventStreamConsumptionResolver.isEventConsumptionComplete(new FinishedProcessingMessage(eventQueue))).thenReturn(true);
        when(event_2.getMetadata()).thenReturn(metadata);

        eventQueue.add(event_1);
        eventQueue.add(event_2);
        final String subscriptionName = "subscriptionName";

        doThrow(nullPointerException).when(transactionalEventProcessor).processWithEventBuffer(event_2, subscriptionName);

        eventQueueConsumer.consumeEventQueue(eventQueue, subscriptionName);

        verify(transactionalEventProcessor).processWithEventBuffer(event_1, subscriptionName);

        verify(logger).error("Failed to process publishedEvent with metadata: {some: metadata}", nullPointerException);
    }
}
