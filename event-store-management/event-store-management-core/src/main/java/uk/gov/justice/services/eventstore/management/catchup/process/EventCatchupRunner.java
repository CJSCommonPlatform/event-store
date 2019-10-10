package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import java.util.UUID;

import javax.inject.Inject;

public class EventCatchupRunner {

    @Inject
    private SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry;

    @Inject
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    public void runEventCatchup(final UUID commandId, final CatchupCommand catchupCommand) {

        subscriptionsDescriptorsRegistry
                .getAll()
                .forEach(subscriptionsDescriptor -> eventCatchupByComponentRunner.runEventCatchupForComponent(
                        commandId,
                        subscriptionsDescriptor,
                        catchupCommand));
    }
}
