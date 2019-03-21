package uk.gov.justice.subscription.registry;

import static java.lang.String.format;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Registry containing {@link SubscriptionsDescriptor}s set
 */
public class SubscriptionsDescriptorsRegistry {
    private final List<SubscriptionsDescriptor> registry;

    public SubscriptionsDescriptorsRegistry(final List<SubscriptionsDescriptor> subscriptionsDescriptors) {
        this.registry = subscriptionsDescriptors;
    }
    /**
     * Return a {@link Subscription} mapped to a subscription name
     *
     * @param subscriptionName the subscription name to look up
     * @return {@link Subscription}
     */
    public Subscription getSubscriptionFor(final String subscriptionName) {
        return registry.stream()
                .map(SubscriptionsDescriptor::getSubscriptions)
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.getName().equals(subscriptionName))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find subscription '%s' in registry", subscriptionName)));
    }

    /**
     * Gets the set of all SubscriptionDescriptors that have been defined
     * @return the set of all SubscriptionDescriptors
     */
    public List<SubscriptionsDescriptor> getAll() {
        return registry;
    }

    /**
     * Return a subscription component name
     *
     * @param subscriptionName the subscription name to look up
     * @return subscription component name
     */
    public String findComponentNameBy(final String subscriptionName) {
        final SubscriptionsDescriptor first = registry
                .stream()
                .filter(subscriptionDescriptorDefinition -> subscriptionNameExists(subscriptionName, subscriptionDescriptorDefinition))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find service component name in registry for subscription '%s' ", subscriptionName)));
        return first.getServiceComponent();
    }

    private boolean subscriptionNameExists(final String subscriptionName, final SubscriptionsDescriptor subscriptionsDescriptor) {
        return subscriptionsDescriptor
                .getSubscriptions()
                .stream()
                .anyMatch(subscription -> subscription.getName().equals(subscriptionName));
    }
}
