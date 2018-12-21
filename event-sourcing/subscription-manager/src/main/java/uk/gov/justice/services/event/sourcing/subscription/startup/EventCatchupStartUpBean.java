package uk.gov.justice.services.event.sourcing.subscription.startup;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
public class EventCatchupStartUpBean {

    @Inject
    EventCatchupProcessorBean eventCatchupProcessorBean;

    @Inject
    SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Resource
    ManagedExecutorService managedExecutorService;

    @Inject
    Logger logger;

    @PostConstruct
    public void start() {
        logger.info("SubscriptionManagerStartUp started");
        final Set<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.getAll();

        subscriptionsDescriptors.forEach(this::startSubscriptions);
    }

    private void startSubscriptions(final SubscriptionsDescriptor subscriptionsDescriptor) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        subscriptionsDescriptor
                .getSubscriptions()
                .forEach(subscription -> startSubscription(componentName, subscription));
    }

    private void startSubscription(final String componentName, final Subscription subscription) {

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                componentName,
                subscription,
                eventCatchupProcessorBean);
        
        managedExecutorService.execute(eventCatchupTask);
    }
}
