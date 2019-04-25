package uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories;

import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import javax.inject.Inject;

public class SubscriptionManagerSelector {

    @Inject
    SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry;

    @Inject
    DefaultSubscriptionManagerFactory defaultSubscriptionManagerFactory;

    @Inject
    BackwardsCompatibleSubscriptionManagerFactory backwardsCompatibleSubscriptionManagerFactory;

    public SubscriptionManager selectFor(final Subscription subscription) {

        final String subscriptionName = subscription.getName();
        final String componentName = subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName);

        if(componentName.contains(EVENT_LISTENER) || componentName.contains(EVENT_INDEXER)) {
            return defaultSubscriptionManagerFactory.create(componentName);
        }

        return backwardsCompatibleSubscriptionManagerFactory.create(componentName);
    }
}
