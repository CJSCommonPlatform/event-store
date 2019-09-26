package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.logging.MdcLogger;
import uk.gov.justice.services.eventstore.management.shuttering.process.CommandHandlerQueueInterrogator;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry;
import uk.gov.justice.services.management.shuttering.startup.ShutteringExecutor;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

@ShutteringExecutor
public class ShutterCommandHandlerObserver {

    @Inject
    private ShutteringRegistry shutteringRegistry;

    @Inject
    private CommandHandlerQueueInterrogator commandHandlerQueueInterrogator;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Inject
    private MdcLogger mdcLogger;

    @Inject
    private Logger logger;

    public void waitForCommandQueueToEmpty(@Observes final ShutteringProcessStartedEvent shutteringProcessStartedEvent) {

        mdcLogger.mdcLoggerConsumer().accept(() -> {

            logger.info("Shuttering Command Handler. Waiting for queue to drain");

            final StopWatch stopWatch = stopWatchFactory.createStartedStopWatch();

            final boolean queueEmpty = commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue();
            if (!queueEmpty) {
                stopWatch.stop();
                throw new ShutteringException(format("Failed to drain command handler queue in %d milliseconds", stopWatch.getTime()));
            }

            logger.info("Command Handler Queue empty");
            shutteringRegistry.markShutteringCompleteFor(getClass(), shutteringProcessStartedEvent.getTarget());
        });
    }
}
