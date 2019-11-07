package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.lang.String.format;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.UUID;

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
            final UUID commandId,
            final SubscriptionsDescriptor subscriptionsDescriptor,
            final CatchupCommand catchupCommand) {

        final String componentName = subscriptionsDescriptor.getServiceComponent();

        if (runCatchupForComponentSelector.shouldRunForThisComponentAndType(componentName, catchupCommand)) {
            subscriptionsDescriptor
                    .getSubscriptions()
                    .forEach(subscription -> runEventCatchupForSubscription(commandId, catchupCommand, componentName, subscription));
        }
    }

    private void runEventCatchupForSubscription(
            final UUID commandId,
            final CatchupCommand catchupCommand,
            final String componentName,
            final Subscription subscription) {

        logger.info(format("Running %s for Component '%s', Subscription '%s'", catchupCommand.getName(), componentName, subscription.getName()));

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                commandId,
                componentName,
                subscription,
                catchupCommand);

        eventCatchupProcessorBean.performEventCatchup(catchupSubscriptionContext);
    }
}
