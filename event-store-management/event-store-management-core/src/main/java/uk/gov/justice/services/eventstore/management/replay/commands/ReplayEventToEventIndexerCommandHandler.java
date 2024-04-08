package uk.gov.justice.services.eventstore.management.replay.commands;

import static uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventIndexerCommand.REPLAY_EVENT_TO_EVENT_INDEXER;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventIndexerCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class ReplayEventToEventIndexerCommandHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @HandlesSystemCommand(REPLAY_EVENT_TO_EVENT_INDEXER)
    public void replayEventToEventIndexer(final ReplayEventToEventIndexerCommand command, final UUID commandId, final UUID commandRuntimeId) {
        final ZonedDateTime startedAt = clock.now();

        stateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                command,
                COMMAND_IN_PROGRESS,
                startedAt,
                "REPLAY_EVENT_TO_EVENT_INDEXER command received"
        ));

        //TODO



        stateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                command,
                COMMAND_COMPLETE,
                clock.now(),
                "REPLAY_EVENT_TO_EVENT_INDEXER command completed"
        ));
    }
}
