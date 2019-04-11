package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventSourceProvider;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventCatchupProcessorFactory {

    @Inject
    private ProcessedEventTrackingService processedEventTrackingService;

    @Inject
    private EventSourceProvider eventSourceProvider;

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
                eventSourceProvider,
                eventStreamConsumerManager,
                catchupStartedForSubscriptionEventFirer,
                catchupCompletedForSubscriptionEventFirer,
                clock
        );
    }
}
