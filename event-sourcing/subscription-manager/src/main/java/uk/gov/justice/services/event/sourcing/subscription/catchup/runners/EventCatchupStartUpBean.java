package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.event.sourcing.subscription.catchup.EventCatchupConfig;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
public class EventCatchupStartUpBean {

    @Inject
    private EventCatchupConfig eventCatchupConfig;

    @Inject
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Inject
    private Logger logger;

    @Inject
    private UtcClock clock;

    @PostConstruct
    public void start() {

        if (eventCatchupConfig.isEventCatchupEnabled()) {
            catchupRequestedEventFirer.fire(new CatchupRequestedEvent(getClass().getSimpleName(), clock.now()));
        } else {
            logger.info("Not performing event Event Catchup: Event catchup disabled");
        }
    }
}
