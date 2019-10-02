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
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

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
    private ProcessedEventTrackingService processedEventTrackingService;

    @Mock
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Mock
    private ConcurrentEventStreamConsumerManager concurrentEventStreamConsumerManager;

    @Mock
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

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
        final String eventSourceName = "event source";
        final String componentName = "EVENT_LISTENER";
        final long eventNumberFrom = 999L;

        final ZonedDateTime catchupStartedAt = new UtcClock().now();
        final ZonedDateTime catchupCompetedAt = catchupStartedAt.plusMinutes(23);

        final Subscription subscription = mock(Subscription.class);
        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                commandId,
                componentName,
                subscription,
                EVENT_CATCHUP,
                systemCommand);

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final List<PublishedEvent> events = asList(publishedEvent_1, publishedEvent_2, publishedEvent_3);

        when(publishedEvent_1.getEventNumber()).thenReturn(of(eventNumberFrom + 1L));
        when(publishedEvent_2.getEventNumber()).thenReturn(of(eventNumberFrom + 2L));
        when(publishedEvent_3.getEventNumber()).thenReturn(of(eventNumberFrom + 3L));

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(clock.now()).thenReturn(catchupStartedAt, catchupCompetedAt);
        when(publishedEventSourceProvider.getPublishedEventSource(eventSourceName)).thenReturn(publishedEventSource);
        when(processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName, componentName)).thenReturn(eventNumberFrom);
        when(publishedEventSource.findEventsSince(eventNumberFrom)).thenReturn(events.stream());
        when(concurrentEventStreamConsumerManager.add(publishedEvent_1, subscriptionName, EVENT_CATCHUP, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_2, subscriptionName, EVENT_CATCHUP, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_3, subscriptionName, EVENT_CATCHUP, commandId)).thenReturn(1);

        eventCatchupProcessor.performEventCatchup(catchupSubscriptionContext);

        final InOrder inOrder = inOrder(
                catchupStartedForSubscriptionEventFirer,
                concurrentEventStreamConsumerManager,
                catchupCompletedForSubscriptionEventFirer);

        inOrder.verify(catchupStartedForSubscriptionEventFirer).fire(new CatchupStartedForSubscriptionEvent(
                commandId,
                subscriptionName,
                EVENT_CATCHUP,
                catchupStartedAt));

        inOrder.verify(concurrentEventStreamConsumerManager).add(publishedEvent_1, subscriptionName, EVENT_CATCHUP, commandId);
        inOrder.verify(concurrentEventStreamConsumerManager).add(publishedEvent_2, subscriptionName, EVENT_CATCHUP, commandId);
        inOrder.verify(concurrentEventStreamConsumerManager).add(publishedEvent_3, subscriptionName, EVENT_CATCHUP, commandId);
        inOrder.verify(concurrentEventStreamConsumerManager).waitForCompletion();

        inOrder.verify(catchupCompletedForSubscriptionEventFirer).fire(new CatchupCompletedForSubscriptionEvent(
                commandId,
                EVENT_CATCHUP,
                subscriptionName,
                eventSourceName,
                componentName,
                systemCommand,
                catchupCompetedAt,
                events.size()));

        verify(logger).info("Catching up from Event Number: " + eventNumberFrom);
        verify(logger).info("Event catch up for Event Number: " + (eventNumberFrom + 1L));
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
        final ZonedDateTime catchupCompetedAt = catchupStartedAt.plusMinutes(23);

        final Subscription subscription = mock(Subscription.class);
        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                commandId,
                componentName,
                subscription,
                EVENT_CATCHUP,
                systemCommand);

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final List<PublishedEvent> events = asList(publishedEvent_1, publishedEvent_2, publishedEvent_3);

        when(publishedEvent_1.getEventNumber()).thenReturn(of(eventNumberFrom + 1L));
        when(publishedEvent_2.getEventNumber()).thenReturn(of(eventNumberFrom + 2L));
        when(publishedEvent_3.getEventNumber()).thenReturn(empty());
        when(publishedEvent_3.getId()).thenReturn(idOfEventWithNoEventNumber);

        when(subscription.getName()).thenReturn(subscriptionName);
        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(clock.now()).thenReturn(catchupStartedAt, catchupCompetedAt);
        when(publishedEventSourceProvider.getPublishedEventSource(eventSourceName)).thenReturn(publishedEventSource);
        when(processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName, componentName)).thenReturn(eventNumberFrom);
        when(publishedEventSource.findEventsSince(eventNumberFrom)).thenReturn(events.stream());
        when(concurrentEventStreamConsumerManager.add(publishedEvent_1, subscriptionName, EVENT_CATCHUP, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_2, subscriptionName, EVENT_CATCHUP, commandId)).thenReturn(1);
        when(concurrentEventStreamConsumerManager.add(publishedEvent_3, subscriptionName, EVENT_CATCHUP, commandId)).thenReturn(1);

        try {
            eventCatchupProcessor.performEventCatchup(catchupSubscriptionContext);
            fail();
        } catch (final MissingEventNumberException expected) {
            assertThat(expected.getMessage(), is("PublishedEvent with id '937f9fd6-3679-4bc2-a73c-6a7b18a651e1' is missing its event number"));
        }
    }
}
