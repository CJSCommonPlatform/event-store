package uk.gov.justice.services.eventstore.management.replay.commands;

import org.slf4j.Logger;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.replay.process.ReplayEventToEventListenerRunner;
import uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventListenerCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.UUID;

import static uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventListenerCommand.REPLAY_EVENT_TO_EVENT_LISTENER;
import static uk.gov.justice.services.jmx.api.domain.CommandState.*;

public class ReplayEventToEventListenerCommandHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Inject
    private ReplayEventToEventListenerRunner replayEventToEventListenerRunner;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(REPLAY_EVENT_TO_EVENT_LISTENER)
    public void replayEventToEventListener(final ReplayEventToEventListenerCommand command, final UUID commandId, final UUID commandRuntimeId) {
        fireEvent(COMMAND_IN_PROGRESS, command, commandId, "ReplayEventToEventListener command received");

        try {
            replayEventToEventListenerRunner.run(commandId, commandRuntimeId);
            fireEvent(COMMAND_COMPLETE, command, commandId, "ReplayEventToEventListener command completed");
        } catch (Exception e) {
            logger.error("ReplayEventToEventListenerCommand failed. commandId {}, commandRuntimeId {}", commandId, commandRuntimeId, e);
            fireEvent(COMMAND_FAILED, command, commandId, "ReplayEventToEventListener command failed");
        }
    }

    private void fireEvent(CommandState commandState, ReplayEventToEventListenerCommand command, UUID commandId, String message) {
        stateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                command,
                commandState,
                clock.now(),
                message
        ));
    }
}
