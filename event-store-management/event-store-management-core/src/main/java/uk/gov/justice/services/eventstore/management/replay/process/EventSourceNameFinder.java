package uk.gov.justice.services.eventstore.management.replay.process;

import uk.gov.justice.services.eventstore.management.catchup.process.PriorityComparatorProvider;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import javax.inject.Inject;
import java.util.stream.Stream;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

public class EventSourceNameFinder {

    private final SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    private final PriorityComparatorProvider priorityComparatorProvider;

    @Inject
    public EventSourceNameFinder(SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry, PriorityComparatorProvider priorityComparatorProvider) {
        this.subscriptionsDescriptorsRegistry = subscriptionsDescriptorsRegistry;
        this.priorityComparatorProvider = priorityComparatorProvider;
    }

    public String getEventSourceNameOfEventListener() {

        return subscriptionsDescriptorsRegistry
                .getAll()
                .stream()
                .filter(subscriptionsDescriptor -> subscriptionsDescriptor.getServiceComponent().contains(EVENT_LISTENER))
                .sorted(priorityComparatorProvider.getSubscriptionDescriptorComparator())
                .flatMap(this::getSubscriptions)
                .findFirst()
                .map(Subscription::getEventSourceName)
                .orElseThrow(() -> new IllegalStateException("No event source name found for event listener"));
    }

    private Stream<Subscription> getSubscriptions(SubscriptionsDescriptor subscriptionsDescriptor) {
        return subscriptionsDescriptor.getSubscriptions()
                .stream()
                .sorted(priorityComparatorProvider.getSubscriptionComparator());
    }
}
