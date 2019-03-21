package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import uk.gov.justice.services.event.sourcing.subscription.catchup.EventCatchupConfig;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
public class EventCatchupStartUpBean {

    @Inject
    EventCatchupConfig eventCatchupConfig;

    @Inject
    EventCatchupRunner eventCatchupRunner;

    @Inject
    Logger logger;


    @PostConstruct
    public void start() {

        if (eventCatchupConfig.isEventCatchupEnabled()) {
            eventCatchupRunner.runEventCatchup();
        } else {
            logger.info("Not performing event Event Catchup: Event catchup disabled");
        }
    }
}
