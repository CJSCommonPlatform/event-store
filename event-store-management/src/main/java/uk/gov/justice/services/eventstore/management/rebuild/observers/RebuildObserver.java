package uk.gov.justice.services.eventstore.management.rebuild.observers;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.MILLIS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildRequestedEvent;
import uk.gov.justice.services.eventstore.management.rebuild.events.RebuildStartedEvent;
import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class RebuildObserver {

    @Inject
    private PublishedEventRebuilder publishedEventRebuilder;

    @Inject
    private Event<RebuildStartedEvent> rebuildStartedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void onRebuildRequested(@Observes final RebuildRequestedEvent rebuildRequestedEvent) {

        final SystemCommand cause = rebuildRequestedEvent.getCause();

        final String causeCommandName = cause.getName();
        logger.info(format("Rebuild requested by '%s' at %tc", causeCommandName, rebuildRequestedEvent.getRebuildRequestedAt()));

        final ZonedDateTime rebuildStartedAt = clock.now();
        logger.info(format("Rebuild for '%s' started at %tc", causeCommandName, rebuildStartedAt));
        rebuildStartedEventFirer.fire(new RebuildStartedEvent(cause, rebuildStartedAt));

        publishedEventRebuilder.rebuild();

        final ZonedDateTime rebuildCompletedAt = clock.now();
        final String format = format("Rebuild for '%s' completed at %tc", causeCommandName, rebuildCompletedAt);
        logger.info(format);
        logger.info(format("Rebuild took %d milliseconds", MILLIS.between(rebuildStartedAt, rebuildCompletedAt)));
    }
}
