package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Comparator.comparingInt;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.Comparator;

public class PriorityComparatorProvider {

    public Comparator<SubscriptionsDescriptor> getSubscriptionDescriptorComparator() {
        return comparingInt(SubscriptionsDescriptor::getPrioritisation);
    }

    public Comparator<Subscription> getSubscriptionComparator() {
        return comparingInt(Subscription::getPrioritisation);
    }
}
