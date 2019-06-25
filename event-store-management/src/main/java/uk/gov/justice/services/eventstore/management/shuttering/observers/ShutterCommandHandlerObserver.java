package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.CommandHandlerQueueChecker;
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
    private CommandHandlerQueueChecker commandHandlerQueueChecker;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Inject
    private Logger logger;

    public void onShutteringProcessStarted(@Observes final ShutteringProcessStartedEvent shutteringProcessStartedEvent) {

        logger.info("Shuttering Command Handler. Waiting for queue to drain");

        final StopWatch stopWatch = stopWatchFactory.createStopWatch();
        stopWatch.start();
        final boolean queueEmpty = commandHandlerQueueChecker.pollUntilEmptyHandlerQueue();
        stopWatch.stop();

        if (!queueEmpty) {
            throw new CommandHandlerShutteringException(format("Failed to drain command handler queue in %d milliseconds", stopWatch.getTime()));
        }

        logger.info("Command Handler Queue empty");
        shutteringRegistry.markShutteringCompleteFor(getClass(), shutteringProcessStartedEvent.getTarget());
    }
}
