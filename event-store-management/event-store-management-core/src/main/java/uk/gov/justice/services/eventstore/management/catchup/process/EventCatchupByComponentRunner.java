package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.lang.String.format;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import javax.inject.Inject;

import org.slf4j.Logger;

public class EventCatchupByComponentRunner {

    @Inject
    private RunCatchupForComponentSelector runCatchupForComponentSelector;

    @Inject
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Inject
    private Logger logger;

    public void runEventCatchupForComponent(
            final SubscriptionsDescriptor subscriptionsDescriptor,
            final CatchupType catchupType,
            final SystemCommand systemCommand) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, catchupType)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> runEventCatchupForSubscription(systemCommand, catchupType, componentName, subscription));
        }
    }

    private void runEventCatchupForSubscription(
            final SystemCommand systemCommand,
            final CatchupType catchupType,
            final String componentName,
            final Subscription subscription) {

        logger.info(format("Running %s catchup for Component '%s', Subscription '%s'", catchupType, componentName, subscription.getName()));

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                componentName,
                subscription,
                catchupType,
                systemCommand);

        eventCatchupProcessorBean.performEventCatchup(catchupSubscriptionContext);
    }
}
