package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.task.EventStreamConsumerManagerFactory;
import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

public class EventCatchupProcessorFactory {

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Inject
    private Event<CatchupStartedForSubscriptionEvent> catchupStartedForSubscriptionEventFirer;

    @Inject
    private Event<CatchupCompletedForSubscriptionEvent> catchupCompletedForSubscriptionEventFirer;

    @Inject
    private EventStreamConsumerManagerFactory eventStreamConsumerManagerFactory;

    @Inject
    private UtcClock clock;

    public EventCatchupProcessor create() {

        final EventStreamConsumerManager eventStreamConsumerManager = eventStreamConsumerManagerFactory.create();

        return new EventCatchupProcessor(
                processedEventTrackingService,
                publishedEventSourceProvider,
                eventStreamConsumerManager,
                catchupStartedForSubscriptionEventFirer,
                catchupCompletedForSubscriptionEventFirer,
                clock,
                LoggerFactory.getLogger(EventCatchupProcessor.class)
        );
    }
}
