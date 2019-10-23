package uk.gov.justice.services.eventstore.management.publishing;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.PublishingCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class EnablePublishingProcessor {

    @Inject
    private PublishingEnabler publishingEnabler;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void enableDisable(final PublishingCommand publishingCommand, final UUID commandId) {

        final String commandName = publishingCommand.getName();
        final String startMessage = format("Running %s", commandName);

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                publishingCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                startMessage
        ));

        logger.info(startMessage);

        publishingEnabler.enableOrDisable(publishingCommand);

        final String completeMessage = format("%s complete", commandName);
        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                publishingCommand,
                COMMAND_COMPLETE,
                clock.now(),
                completeMessage
        ));

        logger.info(completeMessage);
    }
}
