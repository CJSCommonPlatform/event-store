package uk.gov.justice.services.eventsourcing.publishedevent.lifecycle;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.MILLIS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.rebuild.RebuildRequestedEvent;
import uk.gov.justice.services.core.lifecycle.events.rebuild.RebuildStartedEvent;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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

        logger.info(format("Rebuild requested at %tc", rebuildRequestedEvent.getRebuildRequestedAt()));

        final ZonedDateTime rebuildStartedAt = clock.now();
        logger.info(format("Rebuild started at %tc", rebuildStartedAt));
        rebuildStartedEventFirer.fire(new RebuildStartedEvent(rebuildStartedAt));

        publishedEventRebuilder.rebuild();

        final ZonedDateTime rebuildCompletedAt = clock.now();
        final String format = format("Rebuild completed at %tc", rebuildCompletedAt);
        logger.info(format);
        logger.info(format("Rebuild took %d milliseconds", MILLIS.between(rebuildStartedAt, rebuildCompletedAt)));
    }
}
