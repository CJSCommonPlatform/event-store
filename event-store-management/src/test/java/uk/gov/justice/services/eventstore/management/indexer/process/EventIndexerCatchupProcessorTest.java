package uk.gov.justice.services.eventstore.management.indexer.process;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.source.core.PublishedEventSource;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.indexer.events.IndexerCatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.time.ZonedDateTime;
import java.util.List;

import javax.enterprise.event.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventIndexerCatchupProcessorTest {

    @Mock
    private ProcessedEventTrackingService processedEventTrackingService;

    @Mock
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Mock
    private EventStreamConsumerManager eventStreamConsumerManager;

    @Mock
    private Event<IndexerCatchupStartedForSubscriptionEvent> indexerCatchupStartedForSubscriptionEventFirer;

    @Mock
    private Event<IndexerCatchupCompletedForSubscriptionEvent> indexerCatchupCompletedForSubscriptionEventFirer;

    @Mock
    private UtcClock clock;

    private EventIndexerCatchupProcessor eventIndexerCatchupProcessor;

    @Before
    public void createClassUnderTest() {
        eventIndexerCatchupProcessor = new EventIndexerCatchupProcessor(
                processedEventTrackingService,
                publishedEventSourceProvider,
                eventStreamConsumerManager,
                indexerCatchupStartedForSubscriptionEventFirer,
                indexerCatchupCompletedForSubscriptionEventFirer,
                clock
        );
    }

    @Test
    public void shouldFetchAllMissingEventsAndProcess() throws Exception {

        final String subscriptionName = "subscriptionName";
        final String eventSourceName = "event source";
        final String componentName = "EVENT_LISTENER";
        final long eventNumber = 983745987L;

        final ZonedDateTime catchupStartedAt = new UtcClock().now();
        final ZonedDateTime catchupCompetedAt = catchupStartedAt.plusMinutes(23);

        final Subscription subscription = mock(Subscription.class);
        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);
        final IndexerCatchupRequestedEvent indexerCatchupRequestedEvent = mock(IndexerCatchupRequestedEvent.class);
        final IndexerCatchupContext catchupContext = new IndexerCatchupContext(componentName, subscription, indexerCatchupRequestedEvent);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        final JsonEnvelope event_1 = mock(JsonEnvelope.class);
        final JsonEnvelope event_2 = mock(JsonEnvelope.class);
        final JsonEnvelope event_3 = mock(JsonEnvelope.class);

        final List<JsonEnvelope> events = asList(event_1, event_2, event_3);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(clock.now()).thenReturn(catchupStartedAt, catchupCompetedAt);
        when(publishedEventSourceProvider.getPublishedEventSource(eventSourceName)).thenReturn(publishedEventSource);
        when(processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName, componentName)).thenReturn(eventNumber);
        when(publishedEventSource.findEventsSince(eventNumber)).thenReturn(events.stream());
        when(eventStreamConsumerManager.add(event_1, subscriptionName)).thenReturn(1);
        when(eventStreamConsumerManager.add(event_2, subscriptionName)).thenReturn(1);
        when(eventStreamConsumerManager.add(event_3, subscriptionName)).thenReturn(1);
        when(indexerCatchupRequestedEvent.getTarget()).thenReturn(systemCommand);

        eventIndexerCatchupProcessor.performEventIndexerCatchup(catchupContext);

        final InOrder inOrder = inOrder(
                indexerCatchupStartedForSubscriptionEventFirer,
                eventStreamConsumerManager,
                indexerCatchupCompletedForSubscriptionEventFirer);

        inOrder.verify(indexerCatchupStartedForSubscriptionEventFirer).fire(new IndexerCatchupStartedForSubscriptionEvent(
                subscriptionName,
                catchupStartedAt));

        inOrder.verify(eventStreamConsumerManager).add(event_1, subscriptionName);
        inOrder.verify(eventStreamConsumerManager).add(event_2, subscriptionName);
        inOrder.verify(eventStreamConsumerManager).add(event_3, subscriptionName);
        inOrder.verify(eventStreamConsumerManager).waitForCompletion();

        inOrder.verify(indexerCatchupCompletedForSubscriptionEventFirer).fire(new IndexerCatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                systemCommand,
                catchupCompetedAt,
                events.size()));
    }
}
