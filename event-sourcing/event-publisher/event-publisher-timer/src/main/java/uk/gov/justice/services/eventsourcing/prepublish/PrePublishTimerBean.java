package uk.gov.justice.services.eventsourcing.prepublish;

import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerCanceler;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

@Singleton
@Startup
public class PrePublishTimerBean {

    private static final int THRESHOLD = 10;
    private static final String TIMER_JOB_NAME = "event-store.pre-publish-events.job";

    @Resource
    TimerService timerService;

    @Inject
    PrePublishTimerConfig prePublishTimerConfig;

    @Inject
    TimerServiceManager timerServiceManager;

    @Inject
    TimerCanceler timerCanceler;

    @Inject
    PrePublishProcessor prePublishProcessor;

    @PostConstruct
    public void startTimerService() {
        timerCanceler.cancelTimer(TIMER_JOB_NAME, timerService);
        timerServiceManager.createIntervalTimer(
                TIMER_JOB_NAME,
                prePublishTimerConfig.getTimerStartWaitMilliseconds(),
                prePublishTimerConfig.getTimerIntervalMilliseconds(),
                timerService);
    }

    @Timeout
    public void performPrePublish() {

        final int maxEventsPublishedPerIteration = prePublishTimerConfig.getMaxEventsPublishedPerIteration();

        int numberOfEventsPublished = 1;
        while (prePublishProcessor.prePublishNextEvent()) {

            timerServiceManager.cancelOverlappingTimers(TIMER_JOB_NAME, THRESHOLD, timerService);
            numberOfEventsPublished++;

            if (numberOfEventsPublished > maxEventsPublishedPerIteration) {
                break;
            }
        }
    }
}
