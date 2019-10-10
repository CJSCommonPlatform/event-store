package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.lang.String.format;
import static uk.gov.justice.services.management.shuttering.api.ShutteringResult.shutteringFailed;
import static uk.gov.justice.services.management.shuttering.api.ShutteringResult.shutteringSucceeded;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.CommandHandlerQueueInterrogator;
import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

public class CommandHandlerQueueDrainedShutteringExecutor implements ShutteringExecutor {

    @Inject
    private CommandHandlerQueueInterrogator commandHandlerQueueInterrogator;

    @Inject
    private StopWatchFactory stopWatchFactory;

    @Inject
    private Logger logger;

    @Override
    public boolean shouldShutter() {
        return true;
    }

    @Override
    public ShutteringResult shutter(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {

        logger.info("Shuttering Command Handler. Waiting for queue to drain");

        final StopWatch stopWatch = stopWatchFactory.createStartedStopWatch();

        try {
            final boolean queueEmpty = commandHandlerQueueInterrogator.pollUntilEmptyHandlerQueue();

            if (queueEmpty) {

                final String message = "Command Handler Queue drained successfully";
                logger.info(message);

                return shutteringSucceeded(
                        getName(),
                        commandId,
                        message,
                        applicationShutteringCommand
                );
            }

            stopWatch.stop();

            final String message = format("Failed to drain command handler queue in %d milliseconds", stopWatch.getTime());
            logger.error(message);

            return shutteringFailed(
                    getName(),
                    commandId,
                    message,
                    applicationShutteringCommand
            );
        } finally {
            stopWatch.stop();
        }
    }
}
