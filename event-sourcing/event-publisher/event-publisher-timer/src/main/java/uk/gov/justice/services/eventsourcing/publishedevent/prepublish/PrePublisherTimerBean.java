package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

@Singleton
@Startup
@Interceptors(MdcLoggerInterceptor.class)
public class PrePublisherTimerBean {

    private static final String TIMER_JOB_NAME = "event-store.pre-publish-events.job";

    @Resource
    private TimerService timerService;

    @Inject
    private PrePublisherTimerConfig prePublisherTimerConfig;

    @Inject
    private TimerServiceManager timerServiceManager;

    @Inject
    private AsynchronousPrePublisher asynchronousPrePublisher;

    @PostConstruct
    public void startTimerService() {

        timerServiceManager.createIntervalTimer(
                TIMER_JOB_NAME,
                prePublisherTimerConfig.getTimerStartWaitMilliseconds(),
                prePublisherTimerConfig.getTimerIntervalMilliseconds(),
                timerService);
    }

    @Timeout
    public void performPrePublish() {

        if (! prePublisherTimerConfig.isDisabled()) {
            asynchronousPrePublisher.performPrePublish();
        }
    }
}
