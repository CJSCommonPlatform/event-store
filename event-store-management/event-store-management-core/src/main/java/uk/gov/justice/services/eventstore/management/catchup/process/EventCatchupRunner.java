package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.SubscriptionCatchupDetails;

import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class EventCatchupRunner {

    @Inject
    private EventCatchupByComponentRunner eventCatchupByComponentRunner;

    @Inject
    private Event<CatchupStartedEvent> catchupStartedEventFirer;

    @Inject
    private SubscriptionCatchupProvider subscriptionCatchupProvider;

    @Inject
    private UtcClock clock;

    public void runEventCatchup(final UUID commandId, final CatchupCommand catchupCommand) {

        final List<SubscriptionCatchupDetails> subscriptionCatchupDefinitions = subscriptionCatchupProvider.getBySubscription(catchupCommand);

        catchupStartedEventFirer.fire(new CatchupStartedEvent(
                commandId,
                catchupCommand,
                subscriptionCatchupDefinitions,
                clock.now()
        ));

        subscriptionCatchupDefinitions.forEach(catchupFor -> eventCatchupByComponentRunner.runEventCatchupForComponent(
                catchupFor,
                commandId,
                catchupCommand));
    }
}
