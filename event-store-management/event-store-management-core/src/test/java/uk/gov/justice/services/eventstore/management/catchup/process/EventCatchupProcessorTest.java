package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

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
    private ConcurrentEventStreamConsumerManager concurrentEventStreamConsumerManager;

    @Mock
    private MissingEventStreamer missingEventStreamer;

    @Mock
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupProcessor eventCatchupProcessor;

    @Test
    public void shouldFetchAllMissingEventsAndProcess() throws Exception {

        final UUID commandId = randomUUID();
        final String subscriptionName = "subscriptionName";
        final String eventSourceName = "example.event.source";
        final String componentName = "EVENT_LISTENER";
        final long eventNumberFrom = 999L;

        final ZonedDateTime catchupStartedAt = new UtcClock().now();
        final ZonedDateTime catchupCompletedAt = catchupStartedAt.plusMinutes(23);

        final SubscriptionCatchupDetails subscriptionCatchupDetails = mock(SubscriptionCatchupDetails.class);
        final CatchupCommand catchupCommand = new EventCatchupCommand();

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                commandId,
                componentName,
                subscriptionCatchupDetails,
                catchupCommand);

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final List<PublishedEvent> events = asList(publishedEvent_1, publishedEvent_2, publishedEvent_3);

        when(missingEventStreamer.getMissingEvents(eventSourceName, componentName)).thenReturn(events.stream());

        when(publishedEvent_1.getEventNumber()).thenReturn(of(eventNumberFrom + 1L));
        when(publishedEvent_2.getEventNumber()).thenReturn(of(eventNumberFrom + 2L));
        when(publishedEvent_3.getEventNumber()).thenReturn(of(eventNumberFrom + 3L));

        when(subscriptionCatchupDetails.getSubscriptionName()).thenReturn(subscriptionName);
        when(subscriptionCatchupDetails.getEventSourceName()).thenReturn(eventSourceName);
        when(clock.now()).thenReturn(catchupCompletedAt);

        when(concurrentEventStreamConsumerManager.add(publishedEvent_1, subscriptionName, catchupCommand, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_2, subscriptionName, catchupCommand, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_3, subscriptionName, catchupCommand, commandId)).thenReturn(1);

        eventCatchupProcessor.performEventCatchup(catchupSubscriptionContext);

        final InOrder inOrder = inOrder(concurrentEventStreamConsumerManager, catchupCompletedForSubscriptionEventFirer);

        inOrder.verify(concurrentEventStreamConsumerManager).add(publishedEvent_1, subscriptionName, catchupCommand, commandId);
        inOrder.verify(concurrentEventStreamConsumerManager).add(publishedEvent_2, subscriptionName, catchupCommand, commandId);
        inOrder.verify(concurrentEventStreamConsumerManager).add(publishedEvent_3, subscriptionName, catchupCommand, commandId);
        inOrder.verify(concurrentEventStreamConsumerManager).waitForCompletion();

        inOrder.verify(catchupCompletedForSubscriptionEventFirer).fire(new CatchupCompletedForSubscriptionEvent(
                commandId,
                subscriptionName,
                eventSourceName,
                componentName,
                catchupCommand,
                catchupCompletedAt,
                events.size()));

        verify(logger).info("Finding all missing events for event source 'example.event.source', component 'EVENT_LISTENER");
        verify(logger).info("CATCHUP with Event Source: example.event.source for Event Number: " + (eventNumberFrom + 1L));
        verify(logger).info("3 active PublishedEvents queued for publishing");
        verify(logger).info("Waiting for publishing consumer completion...");
    }

    @Test
    public void shouldThrowExceptionIfEventNumberIsAbsentFromPublishedEvent() throws Exception {

        final UUID commandId = randomUUID();
        final String subscriptionName = "subscriptionName";
        final String eventSourceName = "event source";
        final String componentName = "EVENT_LISTENER";
        final long eventNumberFrom = 999L;
        final UUID idOfEventWithNoEventNumber = fromString("937f9fd6-3679-4bc2-a73c-6a7b18a651e1");

        final ZonedDateTime catchupStartedAt = new UtcClock().now();
        final ZonedDateTime catchupCompletedAt = catchupStartedAt.plusMinutes(23);

        final SubscriptionCatchupDetails subscriptionCatchupDetails = mock(SubscriptionCatchupDetails.class);
        final CatchupCommand catchupCommand = mock(CatchupCommand.class);

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                commandId,
                componentName,
                subscriptionCatchupDetails,
                catchupCommand);

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final List<PublishedEvent> events = asList(publishedEvent_1, publishedEvent_2, publishedEvent_3);

        when(missingEventStreamer.getMissingEvents(eventSourceName, componentName)).thenReturn(events.stream());

        when(publishedEvent_1.getEventNumber()).thenReturn(of(eventNumberFrom + 1L));
        when(publishedEvent_2.getEventNumber()).thenReturn(of(eventNumberFrom + 2L));
        when(publishedEvent_3.getEventNumber()).thenReturn(empty());
        when(publishedEvent_3.getId()).thenReturn(idOfEventWithNoEventNumber);

        when(subscriptionCatchupDetails.getSubscriptionName()).thenReturn(subscriptionName);
        when(subscriptionCatchupDetails.getEventSourceName()).thenReturn(eventSourceName);
        when(clock.now()).thenReturn(catchupCompletedAt);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_1, subscriptionName, catchupCommand, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_2, subscriptionName, catchupCommand, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_3, subscriptionName, catchupCommand, commandId)).thenReturn(1);

        try {
            eventCatchupProcessor.performEventCatchup(catchupSubscriptionContext);
            fail();
        } catch (final MissingEventNumberException expected) {
            assertThat(expected.getMessage(), is("PublishedEvent with id '937f9fd6-3679-4bc2-a73c-6a7b18a651e1' is missing its event number"));
        }
    }
}
