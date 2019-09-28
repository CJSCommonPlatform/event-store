package uk.gov.justice.services.eventstore.management.rebuild.observers;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.MILLIS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.eventstore.management.events.rebuild.RebuildCompleteEvent;
import uk.gov.justice.services.eventstore.management.events.rebuild.RebuildRequestedEvent;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class RebuildObserver {

    @Inject
    private PublishedEventRebuilder publishedEventRebuilder;

    @Inject
    private Event<RebuildCompleteEvent> rebuildCompletedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private MdcLogger mdcLogger;

    @Inject
    private Logger logger;

    public void onRebuildRequested(@Observes final RebuildRequestedEvent rebuildRequestedEvent) {

        mdcLogger.mdcLoggerConsumer().accept(() -> {

            final SystemCommand target = rebuildRequestedEvent.getTarget();

            final String commandName = target.getName();
            logger.info(format("Rebuild requested by '%s' command at %tc", commandName, rebuildRequestedEvent.getRebuildRequestedAt()));

            final ZonedDateTime rebuildStartedAt = clock.now();
            logger.info(format("Rebuild for '%s' command started at %tc", commandName, rebuildStartedAt));

            publishedEventRebuilder.rebuild();

            final ZonedDateTime rebuildCompletedAt = clock.now();
            final String format = format("Rebuild for '%s' command completed at %tc", commandName, rebuildCompletedAt);
            logger.info(format);
            logger.info(format("Rebuild took %d milliseconds", MILLIS.between(rebuildStartedAt, rebuildCompletedAt)));

            rebuildCompletedEventFirer.fire(new RebuildCompleteEvent(target, rebuildCompletedAt));
        });
    }
}
