package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NEVER;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.stream.Stream;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class EventCatchupProcessor {

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Inject
    private ConcurrentEventStreamConsumerManager concurrentEventStreamConsumerManager;

    @Inject
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

    @Inject
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @Transactional(NEVER)
    public void performEventCatchup(final CatchupSubscriptionContext catchupSubscriptionContext) {

        final Subscription subscription = catchupSubscriptionContext.getSubscription();
        final String subscriptionName = subscription.getName();
        final CatchupType catchupType = catchupSubscriptionContext.getCatchupType();
        final String eventSourceName = subscription.getEventSourceName();
        final String componentName = catchupSubscriptionContext.getComponentName();
        final SystemCommand systemCommand = catchupSubscriptionContext.getSystemCommand();

        final PublishedEventSource eventSource = publishedEventSourceProvider.getPublishedEventSource(eventSourceName);
        final Long latestProcessedEventNumber = processedEventTrackingService.getLatestProcessedEventNumber(eventSourceName, componentName);

        logger.info("Catching up from Event Number: " + latestProcessedEventNumber);

        catchupStartedForSubscriptionEventFirer.fire(new CatchupStartedForSubscriptionEvent(
                subscriptionName,
                catchupType,
                clock.now()));

        final Stream<PublishedEvent> events = eventSource.findEventsSince(latestProcessedEventNumber);
        final int totalEventsProcessed = events.mapToInt(event -> {

            final Long eventNumber = event.getEventNumber().orElseThrow(() -> new MissingEventNumberException(format("PublishedEvent with id '%s' is missing its event number", event.getId())));

            if (eventNumber % 1000L == 0) {
                logger.info(format("%s catch up for Event Number: %d", catchupType.getName(), eventNumber));
            }

            return concurrentEventStreamConsumerManager.add(event, subscriptionName);

        }).sum();

        concurrentEventStreamConsumerManager.waitForCompletion();

        final CatchupCompletedForSubscriptionEvent event = new CatchupCompletedForSubscriptionEvent(
                catchupType, subscriptionName,
                eventSourceName,
                componentName,
                systemCommand,
                clock.now(),
                totalEventsProcessed);

        catchupCompletedForSubscriptionEventFirer.fire(event);
    }
}
