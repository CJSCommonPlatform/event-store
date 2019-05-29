package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventDeQueuerAndPublisher;
import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventsourcing.util.jee.timer.TimerServiceManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.time.StopWatch;

@Stateless
public class PublisherBean {

    @Inject
    private PublisherTimerConfig publisherTimerConfig;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Inject
    private PublishedEventDeQueuerAndPublisher publishedEventDeQueuerAndPublisher;


    @Asynchronous
    @Transactional(REQUIRES_NEW)
    public void publishAsynchronously() {

        final long maxRuntimeMilliseconds = publisherTimerConfig.getTimerMaxRuntimeMilliseconds();
        final StopWatch stopWatch = stopWatchFactory.createStopWatch();

        stopWatch.start();

        while (publishedEventDeQueuerAndPublisher.deQueueAndPublish()) {

            if (stopWatch.getTime() > maxRuntimeMilliseconds) {
                break;
            }
        }
    }
}
