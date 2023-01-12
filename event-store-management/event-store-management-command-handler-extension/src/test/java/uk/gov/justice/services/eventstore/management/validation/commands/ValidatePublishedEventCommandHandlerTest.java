package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.CommandResult.success;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.CommandResult;
import uk.gov.justice.services.eventstore.management.commands.ValidatePublishedEventsCommand;
import uk.gov.justice.services.eventstore.management.validation.process.EventValidationProcess;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ValidatePublishedEventCommandHandlerTest {

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private EventValidationProcess eventValidationProcess;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @InjectMocks
    private ValidatePublishedEventCommandHandler validatePublishedEventCommandHandler;

    @Test
    public void shouldFireCommandStatusEventsAndRunEventValidation() throws Exception {

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusMinutes(2);

        final UUID commandId = randomUUID();
        final ValidatePublishedEventsCommand validatePublishedEventsCommand = new ValidatePublishedEventsCommand();

        final String successMessage = "success message";
        final CommandResult commandResult = success(commandId, successMessage);

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                eventValidationProcess
        );

        when(clock.now()).thenReturn(startedAt, completedAt);
        when(eventValidationProcess.validateAllPublishedEvents(commandId)).thenReturn(commandResult);

        validatePublishedEventCommandHandler.validateEventsAgainstSchemas(validatePublishedEventsCommand, commandId);

        inOrder.verify(logger).info("Received VALIDATE_EVENTS command");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(eventValidationProcess).validateAllPublishedEvents(commandId);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent endEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(validatePublishedEventsCommand));
        assertThat(startEvent.getStatusChangedAt(), is(startedAt));
        assertThat(startEvent.getMessage(), is("Validation of PublishedEvents against their schemas started"));

        assertThat(endEvent.getCommandId(), is(commandId));
        assertThat(endEvent.getCommandState(), is(commandResult.getCommandState()));
        assertThat(endEvent.getSystemCommand(), is(validatePublishedEventsCommand));
        assertThat(endEvent.getStatusChangedAt(), is(completedAt));
        assertThat(endEvent.getMessage(), is(successMessage));
    }

    @Test
    public void shouldFireFailedEventIfValidationOfEventsFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime failedAt = startedAt.plusMinutes(2);

        final UUID commandId = randomUUID();
        final ValidatePublishedEventsCommand validatePublishedEventsCommand = new ValidatePublishedEventsCommand();

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                eventValidationProcess,
                logger
        );

        when(clock.now()).thenReturn(startedAt, failedAt);
        doThrow(nullPointerException).when(eventValidationProcess).validateAllPublishedEvents(commandId);

        validatePublishedEventCommandHandler.validateEventsAgainstSchemas(validatePublishedEventsCommand, commandId);

        inOrder.verify(logger).info("Received VALIDATE_EVENTS command");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(eventValidationProcess).validateAllPublishedEvents(commandId);
        inOrder.verify(logger).error("Validation of PublishedEvents failed: NullPointerException: Ooops", nullPointerException);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent endEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(validatePublishedEventsCommand));
        assertThat(startEvent.getStatusChangedAt(), is(startedAt));
        assertThat(startEvent.getMessage(), is("Validation of PublishedEvents against their schemas started"));

        assertThat(endEvent.getCommandId(), is(commandId));
        assertThat(endEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(endEvent.getSystemCommand(), is(validatePublishedEventsCommand));
        assertThat(endEvent.getStatusChangedAt(), is(failedAt));
        assertThat(endEvent.getMessage(), is("Validation of PublishedEvents failed: NullPointerException: Ooops"));
    }
}
