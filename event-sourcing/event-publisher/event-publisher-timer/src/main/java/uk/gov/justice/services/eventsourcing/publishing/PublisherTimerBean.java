package uk.gov.justice.services.eventsourcing.publishing;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;

@Singleton
@Startup
public class PublisherTimerBean {

    private static final String TIMER_JOB_NAME = "event-store.de-queue-events-and-publish.job";

    @Resource
    private TimerService timerService;

    @Inject
    private PublisherTimerConfig publisherTimerConfig;

    @Inject
    private TimerServiceManager timerServiceManager;

    @Inject
    private LinkedEventDeQueuerAndPublisher linkedEventDeQueuerAndPublisher;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @PostConstruct
    public void startTimerService() {

        timerServiceManager.createIntervalTimer(
                TIMER_JOB_NAME,
                publisherTimerConfig.getTimerStartWaitMilliseconds(),
                publisherTimerConfig.getTimerIntervalMilliseconds(),
                timerService);
    }

    @Timeout
    public void doDeQueueAndPublish() {

        final long maxRuntimeMilliseconds = publisherTimerConfig.getTimerMaxRuntimeMilliseconds();
        final StopWatch stopWatch = stopWatchFactory.createStopWatch();

        stopWatch.start();

        while (linkedEventDeQueuerAndPublisher.deQueueAndPublish()) {

            if (stopWatch.getTime() > maxRuntimeMilliseconds) {
                break;
            }
        }
    }
}
