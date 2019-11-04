package uk.gov.justice.services.eventstore.management.catchup.observers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.catchup.state.CatchupError;
import uk.gov.justice.services.eventstore.management.catchup.state.CatchupErrorStateManager;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupProcessCompleterTest {

    @Mock
    private CatchupErrorStateManager catchupErrorStateManager;

    @Mock
    private CatchupCompletionEventFirer catchupCompletionEventFirer;

    @InjectMocks
    private CatchupProcessCompleter catchupProcessCompleter;

    @Test
    public void shouldCompleteSuccessfullyIfNoErrors() throws Exception {

        final UUID commandId = randomUUID();
        final CatchupCommand catchupCommand = new EventCatchupCommand();

        when(catchupErrorStateManager.getErrors(catchupCommand)).thenReturn(emptyList());

        catchupProcessCompleter.handleCatchupComplete(commandId, catchupCommand);

        verify(catchupCompletionEventFirer).completeSuccessfully(commandId, catchupCommand);
    }

    @Test
    public void shouldFailCatchupIfCatchupCausedAnyErrors() throws Exception {

        final UUID commandId = randomUUID();
        final CatchupCommand catchupCommand = new EventCatchupCommand();
        final List<CatchupError> errors = asList(mock(CatchupError.class), mock(CatchupError.class));

        when(catchupErrorStateManager.getErrors(catchupCommand)).thenReturn(errors);

        catchupProcessCompleter.handleCatchupComplete(commandId, catchupCommand);

        verify(catchupCompletionEventFirer).failCatchup(commandId, catchupCommand, errors);
    }
}
