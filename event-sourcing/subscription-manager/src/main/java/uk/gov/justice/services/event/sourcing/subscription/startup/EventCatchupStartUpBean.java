package uk.gov.justice.services.event.sourcing.subscription.startup;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.event.sourcing.subscription.startup.task.EventCatchupTask;
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
    EventCatchupConfig eventCatchupConfig;

    @Inject
    Logger logger;

    @PostConstruct
    public void start() {

        if (eventCatchupConfig.isEventCatchupEnabled()) {
            runEventCatchup();
        } else {
            logger.info("Not performing event Event Catchup: Event catchup disabled");
        }
    }

    private void runEventCatchup() {
        logger.info("SubscriptionManagerStartUp started");
        final Set<SubscriptionsDescriptor> subscriptionsDescriptors =
                subscriptionsDescriptorsRegistry.getAll();

        subscriptionsDescriptors.forEach(this::runEventCatchupForSubscription);
    }

    private void runEventCatchupForSubscription(final SubscriptionsDescriptor subscriptionsDescriptor) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (componentName.contains(EVENT_LISTENER)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> runEventCatchupForComponent(componentName, subscription));
        }
    }

    private void runEventCatchupForComponent(final String componentName, final Subscription subscription) {

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(
                componentName,
                subscription,
                eventCatchupProcessorBean);

        managedExecutorService.submit(eventCatchupTask);
    }
}
