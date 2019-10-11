package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;


@RunWith(MockitoJUnitRunner.class)
public class CatchupVerificationProcessRunnerTest {

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
    private CatchupVerificationProcessRunner catchupVerificationProcessRunner;

    @Test
    public void shouldFireCommandStatusEventsAndRunVerification() throws Exception {

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime completedAt = startedAt.plusMinutes(2);

        final UUID commandId = randomUUID();
        final VerifyCatchupCommand verifyCatchupCommand = new VerifyCatchupCommand();

        final InOrder inOrder = inOrder(
                systemCommandStateChangedEventFirer,
                catchupVerificationProcess
        );

        when(clock.now()).thenReturn(startedAt, completedAt);

        catchupVerificationProcessRunner.runVerificationProcess(commandId, verifyCatchupCommand);
        
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(catchupVerificationProcess).runVerification();
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
        assertThat(endEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(endEvent.getSystemCommand(), is(verifyCatchupCommand));
        assertThat(endEvent.getStatusChangedAt(), is(completedAt));
        assertThat(endEvent.getMessage(), is("Verification of catchup complete"));
    }

    @Test
    public void shouldFireFailedEventIfVerificationFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final ZonedDateTime startedAt = new UtcClock().now();
        final ZonedDateTime failedAt = startedAt.plusMinutes(2);

        final UUID commandId = randomUUID();
        final VerifyCatchupCommand verifyCatchupCommand = new VerifyCatchupCommand();

        final InOrder inOrder = inOrder(
                systemCommandStateChangedEventFirer,
                catchupVerificationProcess,
                logger
        );

        when(clock.now()).thenReturn(startedAt, failedAt);
        doThrow(nullPointerException).when(catchupVerificationProcess).runVerification();

        catchupVerificationProcessRunner.runVerificationProcess(commandId, verifyCatchupCommand);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(catchupVerificationProcess).runVerification();
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
