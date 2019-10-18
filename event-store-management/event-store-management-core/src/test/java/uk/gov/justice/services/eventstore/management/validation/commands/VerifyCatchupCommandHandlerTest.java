package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.validation.commands.VerificationCommandResult.success;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.validation.process.CatchupVerificationProcess;
import uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand;
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
public class VerifyCatchupCommandHandlerTest {

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private CatchupVerificationProcess catchupVerificationProcess;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @InjectMocks
    private VerifyCatchupCommandHandler verifyCatchupCommandHandler;

    @Test
    public void shouldFireCommandStatusEventsAndRunVerification() throws Exception {

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusMinutes(2);

        final UUID commandId = randomUUID();
        final VerifyCatchupCommand verifyCatchupCommand = new VerifyCatchupCommand();

        final String successMessage = "success message";
        final VerificationCommandResult verificationCommandResult = success(commandId, successMessage);

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                catchupVerificationProcess
        );

        when(clock.now()).thenReturn(startedAt, completedAt);
        when(catchupVerificationProcess.runVerification(commandId)).thenReturn(verificationCommandResult);

        verifyCatchupCommandHandler.validateCatchup(verifyCatchupCommand, commandId);

        inOrder.verify(logger).info("Received VERIFY_CATCHUP command");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(catchupVerificationProcess).runVerification(commandId);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent endEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(verifyCatchupCommand));
        assertThat(startEvent.getStatusChangedAt(), is(startedAt));
        assertThat(startEvent.getMessage(), is("Verification of catchup started"));

        assertThat(endEvent.getCommandId(), is(commandId));
        assertThat(endEvent.getCommandState(), is(verificationCommandResult.getCommandState()));
        assertThat(endEvent.getSystemCommand(), is(verifyCatchupCommand));
        assertThat(endEvent.getStatusChangedAt(), is(completedAt));
        assertThat(endEvent.getMessage(), is(successMessage));
    }

    @Test
    public void shouldFireFailedEventIfVerificationFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime failedAt = startedAt.plusMinutes(2);

        final UUID commandId = randomUUID();
        final VerifyCatchupCommand verifyCatchupCommand = new VerifyCatchupCommand();

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                catchupVerificationProcess,
                logger
        );

        when(clock.now()).thenReturn(startedAt, failedAt);
        doThrow(nullPointerException).when(catchupVerificationProcess).runVerification(commandId);

        verifyCatchupCommandHandler.validateCatchup(verifyCatchupCommand, commandId);

        inOrder.verify(logger).info("Received VERIFY_CATCHUP command");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(catchupVerificationProcess).runVerification(commandId);
        inOrder.verify(logger).error("Verification of catchup failed: NullPointerException: Ooops", nullPointerException);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent endEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(verifyCatchupCommand));
        assertThat(startEvent.getStatusChangedAt(), is(startedAt));
        assertThat(startEvent.getMessage(), is("Verification of catchup started"));

        assertThat(endEvent.getCommandId(), is(commandId));
        assertThat(endEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(endEvent.getSystemCommand(), is(verifyCatchupCommand));
        assertThat(endEvent.getStatusChangedAt(), is(failedAt));
        assertThat(endEvent.getMessage(), is("Verification of catchup failed: NullPointerException: Ooops"));
    }
}
