package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.inject.Inject;

public class EventCatchupProcessorFactory {

    @Inject
    SubscriptionsRepository subscriptionsRepository;

    public EventCatchupProcessor create(final Subscription subscription, final EventSource eventSource, final EventBufferProcessor eventBufferProcessor) {
        return new EventCatchupProcessor(
                subscription,
                eventSource,
                eventBufferProcessor,
                subscriptionsRepository
        );
    }
}
