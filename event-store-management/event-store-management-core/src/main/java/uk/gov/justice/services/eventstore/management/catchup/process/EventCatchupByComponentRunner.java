package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.lang.String.format;

import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class EventCatchupByComponentRunner {

    @Inject
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Inject
    private Logger logger;

    public void runEventCatchupForComponent(
            final SubscriptionCatchupDetails subscriptionCatchupDefinition,
            final UUID commandId,
            final CatchupCommand catchupCommand) {

        final String componentName = subscriptionCatchupDefinition.getComponentName();
        final String subscriptionName = subscriptionCatchupDefinition.getSubscriptionName();

        logger.info(format("Running %s for Component '%s', Subscription '%s'", catchupCommand.getName(), componentName, subscriptionName));

        final CatchupSubscriptionContext catchupSubscriptionContext = new CatchupSubscriptionContext(
                commandId,
                componentName,
                subscriptionCatchupDefinition,
                catchupCommand);

        eventCatchupProcessorBean.performEventCatchup(catchupSubscriptionContext);
    }
}
