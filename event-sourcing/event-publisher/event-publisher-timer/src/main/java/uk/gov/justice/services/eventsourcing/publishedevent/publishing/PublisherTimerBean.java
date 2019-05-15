package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventDeQueuerAndPublisher;
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
public class PublisherTimerBean {

    private static final int THRESHOLD = 10;
    private static final String TIMER_JOB_NAME = "event-store.de-queue-events-and-publish.job";

    @Resource
    private TimerService timerService;

    @Inject
    private PublisherTimerConfig publisherTimerConfig;

    @Inject
    private TimerServiceManager timerServiceManager;

    @Inject
    private TimerCanceler timerCanceler;

    @Inject
    private PublishedEventDeQueuerAndPublisher publishedEventDeQueuerAndPublisher;

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
        while (publishedEventDeQueuerAndPublisher.deQueueAndPublish()) {
            timerServiceManager.cancelOverlappingTimers(TIMER_JOB_NAME, THRESHOLD, timerService);
        }
    }
}
