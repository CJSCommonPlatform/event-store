package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;

@Stateless
public class AsynchronousPrePublisher {

    @Inject
    private PrePublisherTimerConfig prePublisherTimerConfig;

    @Inject
    private PrePublishProcessor prePublishProcessor;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Asynchronous
    public void performPrePublish() {

        final long maxRuntimeMilliseconds = prePublisherTimerConfig.getTimerMaxRuntimeMilliseconds();
        final StopWatch stopWatch = stopWatchFactory.createStopWatch();

        stopWatch.start();

        while (prePublishProcessor.prePublishNextEvent()) {

            if (stopWatch.getTime() > maxRuntimeMilliseconds) {
                break;
            }
        }
    }
}
