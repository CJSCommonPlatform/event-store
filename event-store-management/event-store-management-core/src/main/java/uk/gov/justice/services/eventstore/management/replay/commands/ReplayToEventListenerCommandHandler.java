package uk.gov.justice.services.eventstore.management.replay.commands;

import org.slf4j.Logger;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventListenerCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.UUID;

import static uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventListenerCommand.REPLAY_EVENT_TO_EVENT_LISTENER;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

public class ReplayToEventListenerCommandHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(REPLAY_EVENT_TO_EVENT_LISTENER)
    public void replayEventToEventListener(final ReplayEventToEventListenerCommand command, final UUID commandId, final UUID commandRuntimeId) {
        final ZonedDateTime startedAt = clock.now();

        stateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                command,
                COMMAND_IN_PROGRESS,
                startedAt,
                "ReplayEventToEventListener command received"
        ));

        //TODO



        stateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                command,
                COMMAND_COMPLETE,
                clock.now(),
                "ReplayEventToEventListener command completed"
        ));
    }
}
