package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventDeQueuerAndPublisher;
import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;

@Stateless
public class AsynchronousPublisher {

    @Inject
    private PublisherTimerConfig publisherTimerConfig;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Inject
    private PublishedEventDeQueuerAndPublisher publishedEventDeQueuerAndPublisher;

    @Asynchronous
    public void doDeQueueAndPublish() {
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
