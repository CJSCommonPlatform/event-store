package uk.gov.justice.services.eventstore.management.catchup.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.CatchupCommand.CATCHUP;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class CatchupCommandHandler {

    @Inject
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(CATCHUP)
    public void catchupEvents(final CatchupCommand catchupCommand) {

        final ZonedDateTime now = clock.now();

        logger.info(format("Received command '%s' at %tr", catchupCommand, now));
        final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(
                catchupCommand,
                now);

        catchupRequestedEventFirer.fire(catchupRequestedEvent);
    }
}
