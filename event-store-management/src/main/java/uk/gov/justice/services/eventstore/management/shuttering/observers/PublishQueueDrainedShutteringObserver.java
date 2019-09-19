package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.logging.MdcLogger;
import uk.gov.justice.services.eventstore.management.shuttering.process.PublishQueueInterrogator;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry;
import uk.gov.justice.services.management.shuttering.startup.ShutteringExecutor;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

@ShutteringExecutor
public class PublishQueueDrainedShutteringObserver {

    @Inject
    private PublishQueueInterrogator publishQueueInterrogator;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Inject
    private ShutteringRegistry shutteringRegistry;

    @Inject
    private MdcLogger mdcLogger;

    @Inject
    private Logger logger;

    public void waitForPublishQueueToEmpty(@Observes final ShutteringProcessStartedEvent shutteringProcessStartedEvent) {

        mdcLogger.mdcLoggerConsumer().accept(() -> {

            logger.info("Waiting for Publish Queue to empty");

            final StopWatch stopWatch = stopWatchFactory.createStartedStopWatch();
            final boolean queueEmpty = publishQueueInterrogator.pollUntilPublishQueueEmpty();

            if (!queueEmpty) {
                stopWatch.stop();
                throw new ShutteringException(format("PublishQueue failed to drain after %d milliseconds", stopWatch.getTime()));
            }

            logger.info("Publish Queue empty");
            shutteringRegistry.markShutteringCompleteFor(getClass(), shutteringProcessStartedEvent.getTarget());
        });
    }
}
