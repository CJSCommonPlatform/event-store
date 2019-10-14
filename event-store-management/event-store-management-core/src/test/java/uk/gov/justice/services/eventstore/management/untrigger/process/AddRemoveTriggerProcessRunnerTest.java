package uk.gov.justice.services.eventstore.management.untrigger.process;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.AddTriggerCommand;
import uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AddRemoveTriggerProcessRunnerTest {

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private AddRemoveTriggerProcessRunner addRemoveTriggerProcessRunner;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldAddTriggerToEventLogTableAndFireTheStatusEvents() throws Exception {

        final UUID commandId = randomUUID();
        final AddTriggerCommand addTriggerCommand = new AddTriggerCommand();
        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusMinutes(2);

        when(clock.now()).thenReturn(startedAt, completedAt);

        addRemoveTriggerProcessRunner.addTriggerToEventLogTable(commandId, addTriggerCommand);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, eventLogTriggerManipulator);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent inProgressEvent = allValues.get(0);
        final SystemCommandStateChangedEvent completeEvent = allValues.get(1);

        assertThat(inProgressEvent.getCommandId(), is(commandId));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getSystemCommand(), is(addTriggerCommand));
        assertThat(inProgressEvent.getStatusChangedAt(), is(startedAt));
        assertThat(inProgressEvent.getMessage(), is("Add trigger to event log table process started"));

        assertThat(completeEvent.getCommandId(), is(commandId));
        assertThat(completeEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(completeEvent.getSystemCommand(), is(addTriggerCommand));
        assertThat(completeEvent.getStatusChangedAt(), is(completedAt));
        assertThat(completeEvent.getMessage(), is("Add trigger to event log table process complete"));
    }

    @Test
    public void shouldFireTheFailedEventIfAddingTriggerFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final UUID commandId = randomUUID();
        final AddTriggerCommand addTriggerCommand = new AddTriggerCommand();
        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime failedAt = startedAt.plusMinutes(2);

        when(clock.now()).thenReturn(startedAt, failedAt);
        doThrow(nullPointerException).when(eventLogTriggerManipulator).addTriggerToEventLogTable();

        addRemoveTriggerProcessRunner.addTriggerToEventLogTable(commandId, addTriggerCommand);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, eventLogTriggerManipulator, logger);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
        inOrder.verify(logger).error("Add trigger to event log table process failed: NullPointerException: Ooops", nullPointerException);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent inProgressEvent = allValues.get(0);
        final SystemCommandStateChangedEvent completeEvent = allValues.get(1);

        assertThat(inProgressEvent.getCommandId(), is(commandId));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getSystemCommand(), is(addTriggerCommand));
        assertThat(inProgressEvent.getStatusChangedAt(), is(startedAt));
        assertThat(inProgressEvent.getMessage(), is("Add trigger to event log table process started"));

        assertThat(completeEvent.getCommandId(), is(commandId));
        assertThat(completeEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(completeEvent.getSystemCommand(), is(addTriggerCommand));
        assertThat(completeEvent.getStatusChangedAt(), is(failedAt));
        assertThat(completeEvent.getMessage(), is("Add trigger to event log table process failed: NullPointerException: Ooops"));
    }

    @Test
    public void shouldRemoveTriggerFromEventLogTableAndFireTheStatusEvents() throws Exception {

        final UUID commandId = randomUUID();
        final RemoveTriggerCommand removeTriggerCommand = new RemoveTriggerCommand();
        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusMinutes(2);

        when(clock.now()).thenReturn(startedAt, completedAt);

        addRemoveTriggerProcessRunner.removeTriggerFromEventLogTable(commandId, removeTriggerCommand);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, eventLogTriggerManipulator);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent inProgressEvent = allValues.get(0);
        final SystemCommandStateChangedEvent completeEvent = allValues.get(1);

        assertThat(inProgressEvent.getCommandId(), is(commandId));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getSystemCommand(), is(removeTriggerCommand));
        assertThat(inProgressEvent.getStatusChangedAt(), is(startedAt));
        assertThat(inProgressEvent.getMessage(), is("Remove trigger from event log table process started"));

        assertThat(completeEvent.getCommandId(), is(commandId));
        assertThat(completeEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(completeEvent.getSystemCommand(), is(removeTriggerCommand));
        assertThat(completeEvent.getStatusChangedAt(), is(completedAt));
        assertThat(completeEvent.getMessage(), is("Remove trigger from event log table process complete"));
    }

    @Test
    public void shouldFireTheFailedEventIfRemovingTriggerFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final UUID commandId = randomUUID();
        final RemoveTriggerCommand removeTriggerCommand = new RemoveTriggerCommand();
        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime failedAt = startedAt.plusMinutes(2);

        when(clock.now()).thenReturn(startedAt, failedAt);
        doThrow(nullPointerException).when(eventLogTriggerManipulator).removeTriggerFromEventLogTable();

        addRemoveTriggerProcessRunner.removeTriggerFromEventLogTable(commandId, removeTriggerCommand);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, eventLogTriggerManipulator, logger);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
        inOrder.verify(logger).error("Remove trigger from event log table process failed: NullPointerException: Ooops", nullPointerException);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent inProgressEvent = allValues.get(0);
        final SystemCommandStateChangedEvent completeEvent = allValues.get(1);

        assertThat(inProgressEvent.getCommandId(), is(commandId));
        assertThat(inProgressEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(inProgressEvent.getSystemCommand(), is(removeTriggerCommand));
        assertThat(inProgressEvent.getStatusChangedAt(), is(startedAt));
        assertThat(inProgressEvent.getMessage(), is("Remove trigger from event log table process started"));

        assertThat(completeEvent.getCommandId(), is(commandId));
        assertThat(completeEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(completeEvent.getSystemCommand(), is(removeTriggerCommand));
        assertThat(completeEvent.getStatusChangedAt(), is(failedAt));
        assertThat(completeEvent.getMessage(), is("Remove trigger from event log table process failed: NullPointerException: Ooops"));
    }
}

