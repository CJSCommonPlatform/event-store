package uk.gov.justice.services.eventsourcing.prepublish;

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
public class PrePublishTimerBean {

    private static final String TIMER_JOB_NAME = "event-store.pre-publish-events.job";

    @Resource
    private TimerService timerService;

    @Inject
    private PrePublishTimerConfig prePublishTimerConfig;

    @Inject
    private TimerServiceManager timerServiceManager;

    @Inject
    private PrePublishProcessor prePublishProcessor;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @PostConstruct
    public void startTimerService() {

        timerServiceManager.createIntervalTimer(
                TIMER_JOB_NAME,
                prePublishTimerConfig.getTimerStartWaitMilliseconds(),
                prePublishTimerConfig.getTimerIntervalMilliseconds(),
                timerService);
    }

    @Timeout
    public void performPrePublish() {

        final long maxRuntimeMilliseconds = prePublishTimerConfig.getTimerMaxRuntimeMilliseconds();
        final StopWatch stopWatch = stopWatchFactory.createStopWatch();

        stopWatch.start();

        while (prePublishProcessor.prePublishNextEvent()) {

            if (stopWatch.getTime() > maxRuntimeMilliseconds) {
                break;
            }
        }
    }
}
