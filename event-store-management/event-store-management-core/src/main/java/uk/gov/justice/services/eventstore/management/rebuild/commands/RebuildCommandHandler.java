package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.MILLIS;
import static uk.gov.justice.services.jmx.api.command.RebuildCommand.REBUILD;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

public class RebuildCommandHandler {

    @Inject
    private PublishedEventRebuilder publishedEventRebuilder;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(REBUILD)
    public void doRebuild(final RebuildCommand rebuildCommand, final UUID commandId) {
        final String commandName = rebuildCommand.getName();
        final ZonedDateTime rebuildStartedAt = clock.now();
        final String startMessage = format("%s started at %tc", commandName, rebuildStartedAt);

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                rebuildCommand,
                COMMAND_IN_PROGRESS,
                rebuildStartedAt,
                startMessage
        ));

        logger.info(startMessage);

        try {
            publishedEventRebuilder.rebuild();

            final ZonedDateTime rebuildCompletedAt = clock.now();
            logger.info(format("%s command completed at %tc", commandName, rebuildCompletedAt));
            final String endMessage = format("Rebuild took %d milliseconds", MILLIS.between(rebuildStartedAt, rebuildCompletedAt));

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    rebuildCommand,
                    COMMAND_COMPLETE,
                    rebuildCompletedAt,
                    endMessage
            ));

            logger.info(endMessage);

        } catch (final Exception e) {

            logger.error(format("%s failed", commandName), e);

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    rebuildCommand,
                    COMMAND_FAILED,
                    clock.now(),
                    format("Rebuild failed: %s: %s", e.getClass().getSimpleName(), e.getMessage())
            ));
        }
    }
}
