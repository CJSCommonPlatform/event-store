package uk.gov.justice.services.event.sourcing.subscription.startup;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
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
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private EventSourceProvider eventSourceProvider;

    @Mock
    private EventStreamConsumerManager eventStreamConsumerManager;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupProcessor eventCatchupProcessor;

    @Test
    public void shouldFetchAllMissingEventsAndProcess() throws Exception {

        final String eventSourceName = "event source";
        final long eventNumber = 983745987L;

        final Subscription subscription = mock(Subscription.class);
        final EventSource eventSource = mock(EventSource.class);

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);
        final JsonEnvelope event_3 = mock(JsonEnvelope.class);

        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(eventSourceProvider.getEventSource(eventSourceName)).thenReturn(eventSource);
        when(subscriptionsRepository.getOrInitialiseCurrentEventNumber(eventSourceName)).thenReturn(eventNumber);
        when(eventSource.findEventsSince(eventNumber)).thenReturn(Stream.of(event_1, event_2, event_3));

        eventCatchupProcessor.performEventCatchup(subscription);

        final InOrder inOrder = inOrder(logger, eventStreamConsumerManager);

        inOrder.verify(logger).info("Event catchup started");
        inOrder.verify(logger).info("Performing catchup of events...");
        inOrder.verify(eventStreamConsumerManager).add(event_1);
        inOrder.verify(eventStreamConsumerManager).add(event_2);
        inOrder.verify(eventStreamConsumerManager).add(event_3);
        inOrder.verify(logger).info("Event catchup retrieved and processed 3 new events");
        inOrder.verify(logger).info("Event catchup complete");
    }
}
