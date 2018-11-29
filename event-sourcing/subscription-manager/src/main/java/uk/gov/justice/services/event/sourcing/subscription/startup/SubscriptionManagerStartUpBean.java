package uk.gov.justice.services.event.sourcing.subscription.startup;

import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.SubscriptionNameQualifier;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class SubscriptionManagerStartUpBean {

    @Inject
    Logger LOGGER = LoggerFactory.getLogger(SubscriptionManagerStartUpBean.class);

    @Inject
    @Any
    Instance<SubscriptionManager> subscriptionManagers;

    @Inject
    SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Resource
    ManagedExecutorService managedExecutorService;

    @PostConstruct
    public void start() {
        LOGGER.info("SubscriptionManagerStartUp started");
        final Set<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.subscriptionsDescriptors();

        subscriptionsDescriptors.forEach(this::startSubscriptions);
    }

    private void startSubscriptions(final SubscriptionsDescriptor subscriptionDescriptorDefinition) {
        subscriptionDescriptorDefinition.getSubscriptions().forEach(this::startSubscription);
    }

    private void startSubscription(final Subscription subscription) {

        final SubscriptionNameQualifier subscriptionNameQualifier = new SubscriptionNameQualifier(subscription.getName());
        final SubscriptionManager subscriptionManager = subscriptionManagers.select(subscriptionNameQualifier).get();

        managedExecutorService.execute(new StartSubscriptionTask(subscriptionManager));
    }
}
