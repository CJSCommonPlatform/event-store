package uk.gov.justice.services.eventstore.management.replay.commands;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventIndexerCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnit44Runner;

@RunWith(MockitoJUnit44Runner.class)
public class ReplayEventToEventIndexerCommandHandlerTest {

    @Mock
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private ReplayEventToEventIndexerCommandHandler replayEventToEventIndexerCommandHandler;

    @Test
    public void shouldSendSingleEventToEventIndexer() throws Exception {

        final ReplayEventToEventIndexerCommand command = new ReplayEventToEventIndexerCommand();
        final UUID commandId = randomUUID();
        final UUID commandRuntimeId = randomUUID();

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusSeconds(2);

        when(clock.now()).thenReturn(startedAt, completedAt);

        replayEventToEventIndexerCommandHandler.replayEventToEventIndexer(
                command,
                commandId,
                commandRuntimeId
        );

        final ArgumentCaptor<SystemCommandStateChangedEvent> argumentCaptor = forClass(SystemCommandStateChangedEvent.class);

        verify(stateChangedEventFirer, times(2)).fire(argumentCaptor.capture());

        final List<SystemCommandStateChangedEvent> stateChangedEvents = argumentCaptor.getAllValues();

        final SystemCommandStateChangedEvent commandInProgressEvent = stateChangedEvents.get(0);
        final SystemCommandStateChangedEvent commandCompleteEvent = stateChangedEvents.get(1);

        assertThat(commandInProgressEvent.getCommandId(), is(commandId));
        assertThat(commandInProgressEvent.getSystemCommand(), is(command));
        assertThat(commandInProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(commandInProgressEvent.getMessage(), is("REPLAY_EVENT_TO_EVENT_INDEXER command received"));
        assertThat(commandInProgressEvent.getStatusChangedAt(), is(startedAt));

        assertThat(commandCompleteEvent.getCommandId(), is(commandId));
        assertThat(commandCompleteEvent.getSystemCommand(), is(command));
        assertThat(commandCompleteEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(commandCompleteEvent.getMessage(), is("REPLAY_EVENT_TO_EVENT_INDEXER command completed"));
        assertThat(commandCompleteEvent.getStatusChangedAt(), is(completedAt));
    }
}