package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.event.source.subscriptions.repository.jdbc.SubscriptionsRepository;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventBufferProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import javax.inject.Inject;

import org.slf4j.LoggerFactory;

public class EventCatchupProcessorFactory {

    @Inject
    SubscriptionsRepository subscriptionsRepository;

    public EventCatchupProcessor create(
            final Subscription subscription,
            final EventSource eventSource,
            final EventBufferProcessor eventBufferProcessor) {

        final TransactionalEventProcessor transactionalEventProcessor = new TransactionalEventProcessor(eventBufferProcessor);

        return new EventCatchupProcessor(
                subscription,
                eventSource,
                subscriptionsRepository,
                transactionalEventProcessor,
                getLogger(EventCatchupProcessor.class)
        );
    }
}
