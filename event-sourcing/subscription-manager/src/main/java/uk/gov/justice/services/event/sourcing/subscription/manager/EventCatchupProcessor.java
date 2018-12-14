package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

public class EventCatchupProcessor {

    private final Subscription subscription;
    private final SubscriptionsRepository subscriptionsRepository;
    private final EventSource eventSource;
    private final EventBufferProcessor eventBufferProcessor;

    public EventCatchupProcessor(
            final Subscription subscription,
            final EventSource eventSource,
            final EventBufferProcessor eventBufferProcessor,
            final SubscriptionsRepository subscriptionsRepository) {
        this.subscription = subscription;
        this.subscriptionsRepository = subscriptionsRepository;
        this.eventSource = eventSource;
        this.eventBufferProcessor = eventBufferProcessor;
    }

    public void performEventCatchup() {
        final long eventNumber = subscriptionsRepository.getOrInitialiseCurrentEventNumber(subscription.getName());
        eventSource.findEventsSince(eventNumber)
                .forEach(eventBufferProcessor::processWithEventBuffer);
    }
}
