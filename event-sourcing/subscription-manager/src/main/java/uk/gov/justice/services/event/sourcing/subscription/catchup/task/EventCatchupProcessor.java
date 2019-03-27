package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.stream.Stream;

import javax.enterprise.event.Event;
import javax.transaction.Transactional;

public class EventCatchupProcessor {

    private final ProcessedEventTrackingService processedEventTrackingService;
    private final EventSourceProvider eventSourceProvider;
    private final EventStreamConsumerManager eventStreamConsumerManager;
    private final Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;
    private final Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;
    private final UtcClock clock;

    public EventCatchupProcessor(
            final ProcessedEventTrackingService processedEventTrackingService,
            final EventSourceProvider eventSourceProvider,
            final EventStreamConsumerManager eventStreamConsumerManager,
            final Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer,
            final Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer,
            final UtcClock clock) {
        this.processedEventTrackingService = processedEventTrackingService;
        this.eventSourceProvider = eventSourceProvider;
        this.eventStreamConsumerManager = eventStreamConsumerManager;
        this.catchupStartedForSubscriptionEventFirer = catchupStartedForSubscriptionEventFirer;
        this.catchupCompletedForSubscriptionEventFirer = catchupCompletedForSubscriptionEventFirer;
        this.clock = clock;
    }

    @Transactional(NOT_SUPPORTED)
    public void performEventCatchup(final Subscription subscription) {

        final String eventSourceName = subscription.getEventSourceName();
        final EventSource eventSource = eventSourceProvider.getEventSource(eventSourceName);
        final Long latestProcessedEventNumber = processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName);

        catchupStartedForSubscriptionEventFirer.fire(new CatchupStartedForSubscriptionEvent(
                eventSourceName,
                clock.now()));

        final Stream<JsonEnvelope> events = eventSource.findEventsSince(latestProcessedEventNumber);
        final int totalEventsProcessed = events.mapToInt(eventStreamConsumerManager::add).sum();

        eventStreamConsumerManager.waitForCompletion();

        final CatchupCompletedForSubscriptionEvent event = new CatchupCompletedForSubscriptionEvent(
                eventSourceName,
                totalEventsProcessed,
                clock.now());

        catchupCompletedForSubscriptionEventFirer.fire(event);
    }
}
