package uk.gov.justice.services.eventstore.management.replay.commands;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.ReplayEventToEventListenerCommand;
import uk.gov.justice.services.eventstore.management.replay.process.ReplayEventToEventListenerRunner;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import javax.enterprise.event.Event;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.jmx.api.domain.CommandState.*;

@RunWith(MockitoJUnitRunner.class)
public class ReplayEventToEventListenerCommandHandlerTest {

    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID COMMAND_RUNTIME_ID = UUID.randomUUID();

    private static final ReplayEventToEventListenerCommand COMMAND = new ReplayEventToEventListenerCommand();
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneOffset.UTC);

    @Mock
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Mock
    private ReplayEventToEventListenerRunner replayEventToEventListenerRunner;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> eventCaptor;

    @InjectMocks
    private ReplayEventToEventListenerCommandHandler replayEventToEventListenerCommandHandler;

    @Test
    public void onSuccessShouldFireInProgressAndCompletedSystemCommands() {
        when(clock.now()).thenReturn(NOW);

        replayEventToEventListenerCommandHandler.replayEventToEventListener(COMMAND, COMMAND_ID, COMMAND_RUNTIME_ID);

        verify(replayEventToEventListenerRunner).run(COMMAND_ID, COMMAND_RUNTIME_ID);

        verify(stateChangedEventFirer, times(2)).fire(eventCaptor.capture());
        final List<SystemCommandStateChangedEvent> actualEvents = eventCaptor.getAllValues();
        final SystemCommandStateChangedEvent inProgressEvent = actualEvents.get(0);

        assertThat(inProgressEvent.getCommandId(), is(COMMAND_ID));
        assertThat(inProgressEvent.getSystemCommand(), is(COMMAND));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getMessage(), is("ReplayEventToEventListener command received"));
        assertThat(inProgressEvent.getStatusChangedAt(), is(NOW));

        final SystemCommandStateChangedEvent completedEvent = actualEvents.get(1);
        assertThat(completedEvent.getCommandId(), is(COMMAND_ID));
        assertThat(completedEvent.getSystemCommand(), is(COMMAND));
        assertThat(completedEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(completedEvent.getMessage(), is("ReplayEventToEventListener command completed"));
        assertThat(completedEvent.getStatusChangedAt(), is(NOW));
    }

    @Test
    public void onSuccessShouldFireInProgressAndFailedyStemCommands() {
        final RuntimeException exception = new RuntimeException();
        when(clock.now()).thenReturn(NOW);
        doThrow(exception).when(replayEventToEventListenerRunner).run(any(), any());

        replayEventToEventListenerCommandHandler.replayEventToEventListener(COMMAND, COMMAND_ID, COMMAND_RUNTIME_ID);

        verify(stateChangedEventFirer, times(2)).fire(eventCaptor.capture());
        final List<SystemCommandStateChangedEvent> actualEvents = eventCaptor.getAllValues();
        final SystemCommandStateChangedEvent inProgressEvent = actualEvents.get(0);

        assertThat(inProgressEvent.getCommandId(), is(COMMAND_ID));
        assertThat(inProgressEvent.getSystemCommand(), is(COMMAND));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getMessage(), is("ReplayEventToEventListener command received"));
        assertThat(inProgressEvent.getStatusChangedAt(), is(NOW));

        final SystemCommandStateChangedEvent completedEvent = actualEvents.get(1);
        assertThat(completedEvent.getCommandId(), is(COMMAND_ID));
        assertThat(completedEvent.getSystemCommand(), is(COMMAND));
        assertThat(completedEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(completedEvent.getMessage(), is("ReplayEventToEventListener command failed"));

        verify(logger).error("ReplayEventToEventListenerCommand failed. commandId {}, commandRuntimeId {}", COMMAND_ID, COMMAND_RUNTIME_ID, exception);
    }
}