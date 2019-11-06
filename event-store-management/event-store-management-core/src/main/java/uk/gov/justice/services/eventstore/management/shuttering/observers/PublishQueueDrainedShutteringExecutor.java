package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.lang.String.format;
import static uk.gov.justice.services.management.shuttering.api.ShutteringResult.shutteringFailed;
import static uk.gov.justice.services.management.shuttering.api.ShutteringResult.shutteringSucceeded;

import uk.gov.justice.services.eventsourcing.util.jee.timer.StopWatchFactory;
import uk.gov.justice.services.eventstore.management.shuttering.process.PublishQueueInterrogator;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;
import uk.gov.justice.services.management.shuttering.commands.ApplicationShutteringCommand;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

public class PublishQueueDrainedShutteringExecutor implements ShutteringExecutor {

    @Inject
    private PublishQueueInterrogator publishQueueInterrogator;

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

        logger.info("Waiting for Publish Queue to empty");
        final StopWatch stopWatch = stopWatchFactory.createStartedStopWatch();

        try {
            final boolean queueEmpty = publishQueueInterrogator.pollUntilPublishQueueEmpty();

            if (queueEmpty) {
                final String message = "Publish Queue drained successfully";
                logger.info(message);

                return shutteringSucceeded(
                        getName(),
                        commandId,
                        message,
                        applicationShutteringCommand
                );
            }

            stopWatch.stop();

            final String message = format("PublishQueue failed to drain after %d milliseconds", stopWatch.getTime());

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
