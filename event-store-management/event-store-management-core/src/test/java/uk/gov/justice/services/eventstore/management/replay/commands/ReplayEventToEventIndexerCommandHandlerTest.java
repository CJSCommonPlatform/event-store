package uk.gov.justice.services.eventstore.management.replay.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_INDEXER;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventIndexerCommand;
import uk.gov.justice.services.eventstore.management.replay.process.ReplayEventToComponentRunner;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ReplayEventToEventIndexerCommandHandlerTest {

    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID COMMAND_RUNTIME_ID = UUID.randomUUID();

    private static final ReplayEventToEventIndexerCommand COMMAND = new ReplayEventToEventIndexerCommand();
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneOffset.UTC);

    @Mock
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Mock
    private ReplayEventToComponentRunner replayEventToComponentRunner;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> eventCaptor;

    @InjectMocks
    private ReplayEventToEventIndexerCommandHandler replayEventToEventIndexerCommandHandler;

    @Test
    public void onSuccessShouldFireInProgressAndCompletedSystemCommands() {
        when(clock.now()).thenReturn(NOW);

        replayEventToEventIndexerCommandHandler.replayEventToEventIndexer(COMMAND, COMMAND_ID, COMMAND_RUNTIME_ID);

        verify(replayEventToComponentRunner).run(COMMAND_ID, COMMAND_RUNTIME_ID, EVENT_INDEXER);

        verify(stateChangedEventFirer, times(2)).fire(eventCaptor.capture());
        final List<SystemCommandStateChangedEvent> actualEvents = eventCaptor.getAllValues();
        final SystemCommandStateChangedEvent inProgressEvent = actualEvents.get(0);

        assertThat(inProgressEvent.getCommandId(), is(COMMAND_ID));
        assertThat(inProgressEvent.getSystemCommand(), is(COMMAND));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getMessage(), is("REPLAY_EVENT_TO_EVENT_INDEXER command received"));
        assertThat(inProgressEvent.getStatusChangedAt(), is(NOW));

        final SystemCommandStateChangedEvent completedEvent = actualEvents.get(1);
        assertThat(completedEvent.getCommandId(), is(COMMAND_ID));
        assertThat(completedEvent.getSystemCommand(), is(COMMAND));
        assertThat(completedEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(completedEvent.getMessage(), is("REPLAY_EVENT_TO_EVENT_INDEXER command completed"));
        assertThat(completedEvent.getStatusChangedAt(), is(NOW));
    }

    @Test
    public void onSuccessShouldFireInProgressAndFailedyStemCommands() {
        final RuntimeException exception = new RuntimeException();
        when(clock.now()).thenReturn(NOW);
        doThrow(exception).when(replayEventToComponentRunner).run(any(), any(), any());

        replayEventToEventIndexerCommandHandler.replayEventToEventIndexer(COMMAND, COMMAND_ID, COMMAND_RUNTIME_ID);

        verify(stateChangedEventFirer, times(2)).fire(eventCaptor.capture());
        final List<SystemCommandStateChangedEvent> actualEvents = eventCaptor.getAllValues();
        final SystemCommandStateChangedEvent inProgressEvent = actualEvents.get(0);

        assertThat(inProgressEvent.getCommandId(), is(COMMAND_ID));
        assertThat(inProgressEvent.getSystemCommand(), is(COMMAND));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getMessage(), is("REPLAY_EVENT_TO_EVENT_INDEXER command received"));
        assertThat(inProgressEvent.getStatusChangedAt(), is(NOW));

        final SystemCommandStateChangedEvent completedEvent = actualEvents.get(1);
        assertThat(completedEvent.getCommandId(), is(COMMAND_ID));
        assertThat(completedEvent.getSystemCommand(), is(COMMAND));
        assertThat(completedEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(completedEvent.getMessage(), is("REPLAY_EVENT_TO_EVENT_INDEXER command failed"));

        verify(logger).error("REPLAY_EVENT_TO_EVENT_INDEXER failed. commandId {}, commandRuntimeId {}", COMMAND_ID, COMMAND_RUNTIME_ID, exception);
    }
}