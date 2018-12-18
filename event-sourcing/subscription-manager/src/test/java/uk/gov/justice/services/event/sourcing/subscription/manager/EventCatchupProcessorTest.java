package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class EventCatchupProcessorTest {

    @Mock
    private Subscription subscription;

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private EventSource eventSource;

    @Mock
    private TransactionalEventProcessor transactionalEventProcessor;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupProcessor eventCatchupProcessor;

    @Test
    public void shouldFetchAllMissingEventsAndProcess() throws Exception {

        final String subscriptionName = "subscriptionName";
        final long eventNumber = 983745987L;

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);
        final JsonEnvelope event_3 = mock(JsonEnvelope.class);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscriptionName)).thenReturn(eventNumber);
        when(eventSource.findEventsSince(eventNumber)).thenReturn(Stream.of(event_1, event_2, event_3));
        when(transactionalEventProcessor.processWithEventBuffer(any(JsonEnvelope.class))).thenReturn(1);

        eventCatchupProcessor.performEventCatchup();

        final InOrder inOrder = inOrder(logger, transactionalEventProcessor);

        inOrder.verify(logger).info("Event catchup started");
        inOrder.verify(logger).info("Performing catchup of events...");
        inOrder.verify(transactionalEventProcessor).processWithEventBuffer(event_1);
        inOrder.verify(transactionalEventProcessor).processWithEventBuffer(event_2);
        inOrder.verify(transactionalEventProcessor).processWithEventBuffer(event_3);
        inOrder.verify(logger).info("Event catchup retrieved and processed 3 new events");
        inOrder.verify(logger).info("Event catchup complete");
    }
}
