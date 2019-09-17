package uk.gov.justice.services.eventstore.management.catchup.process;

import static javax.transaction.Transactional.TxType.NEVER;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.source.core.PublishedEventSource;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.stream.Stream;

import javax.enterprise.event.Event;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class EventCatchupProcessor {

    private final ProcessedEventTrackingService processedEventTrackingService;
    private final PublishedEventSourceProvider publishedEventSourceProvider;
    private final EventStreamConsumerManager eventStreamConsumerManager;
    private final Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;
    private final Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;
    private final UtcClock clock;
    private final Logger logger;

    public EventCatchupProcessor(
            final ProcessedEventTrackingService processedEventTrackingService,
            final PublishedEventSourceProvider publishedEventSourceProvider,
            final EventStreamConsumerManager eventStreamConsumerManager,
            final Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer,
            final Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer,
            final UtcClock clock,
            final Logger logger) {
        this.processedEventTrackingService = processedEventTrackingService;
        this.publishedEventSourceProvider = publishedEventSourceProvider;
        this.eventStreamConsumerManager = eventStreamConsumerManager;
        this.catchupStartedForSubscriptionEventFirer = catchupStartedForSubscriptionEventFirer;
        this.catchupCompletedForSubscriptionEventFirer = catchupCompletedForSubscriptionEventFirer;
        this.clock = clock;
        this.logger = logger;
    }

    @Transactional(NEVER)
    public void performEventCatchup(final CatchupContext catchupContext) {

        final Subscription subscription = catchupContext.getSubscription();
        final String subscriptionName = subscription.getName();
        final String eventSourceName = subscription.getEventSourceName();
        final String componentName = catchupContext.getComponentName();
        final CatchupRequestedEvent catchupRequestedEvent = catchupContext.getCatchupRequestedEvent();

        final PublishedEventSource eventSource = publishedEventSourceProvider.getPublishedEventSource(eventSourceName);
        final Long latestProcessedEventNumber = processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName, componentName);

        logger.info("Catching up from Event Number: " + latestProcessedEventNumber);

        catchupStartedForSubscriptionEventFirer.fire(new CatchupStartedForSubscriptionEvent(
                subscriptionName,
                clock.now()));

        final Stream<JsonEnvelope> events = eventSource.findEventsSince(latestProcessedEventNumber);
        final int totalEventsProcessed = events.mapToInt(event -> {

            final Long eventNumber = event.metadata().eventNumber().get();

            if (eventNumber % 1000L == 0) {
                logger.info("Starting catch up for Event Number: " + eventNumber);
            }

            return eventStreamConsumerManager.add(event, subscriptionName);

        }).sum();

        eventStreamConsumerManager.waitForCompletion();

        final CatchupCompletedForSubscriptionEvent event = new CatchupCompletedForSubscriptionEvent(
                subscriptionName,
                eventSourceName,
                componentName,
                catchupRequestedEvent.getTarget(),
                clock.now(),
                totalEventsProcessed);

        catchupCompletedForSubscriptionEventFirer.fire(event);
    }
}
