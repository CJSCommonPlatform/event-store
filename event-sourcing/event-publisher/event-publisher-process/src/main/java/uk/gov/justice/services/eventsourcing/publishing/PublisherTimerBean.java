package uk.gov.justice.services.eventsourcing.publishing;

import uk.gov.justice.services.eventsourcing.timer.TimerCanceler;
import uk.gov.justice.services.eventsourcing.timer.TimerServiceManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

@Singleton
@Startup
public class PublisherTimerBean {

    private static final int THRESHOLD = 10;
    private static final String TIMER_JOB_NAME = "framework.de-queue-events-and-publish.job";

    @Resource
    TimerService timerService;

    @Inject
    PublisherTimerConfig publisherTimerConfig;

    @Inject
    TimerServiceManager timerServiceManager;

    @Inject
    TimerCanceler timerCanceler;

    @Inject
    EventDeQueuerAndPublisher eventDeQueuerAndPublisher;

    @PostConstruct
    public void startTimerService() {
        timerCanceler.cancelTimer(TIMER_JOB_NAME, timerService);
        timerServiceManager.createIntervalTimer(
                TIMER_JOB_NAME,
                publisherTimerConfig.getTimerStartWaitMilliseconds(),
                publisherTimerConfig.getTimerIntervalMilliseconds(),
                timerService);
    }

    @Timeout
    public void doDeQueueAndPublish() {

        while (eventDeQueuerAndPublisher.deQueueAndPublish()) {
            timerServiceManager.cancelOverlappingTimers(TIMER_JOB_NAME, THRESHOLD, timerService);
        }
    }
}
