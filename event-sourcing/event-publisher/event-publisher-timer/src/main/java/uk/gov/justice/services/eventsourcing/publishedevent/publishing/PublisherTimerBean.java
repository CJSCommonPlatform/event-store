package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

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
public class PublisherTimerBean {

    private static final String TIMER_JOB_NAME = "event-store.de-queue-events-and-publish.job";

    @Resource
    private TimerService timerService;

    @Inject
    private PublisherTimerConfig publisherTimerConfig;

    @Inject
    private TimerServiceManager timerServiceManager;

    @Inject
    private AsynchronousPublisher asynchronousPublisher;

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

        if (! publisherTimerConfig.isDisabled()) {
            asynchronousPublisher.doDeQueueAndPublish();
        }
    }
}
