package uk.gov.justice.services.eventstore.management.catchup.state;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupErrorStateManagerTest {


    @InjectMocks
    private CatchupErrorStateManager catchupErrorStateManager;

    @Test
    public void shouldKeepListOfAllCatchupErrorsForEventCatchup() throws Exception {

        final EventCatchupCommand eventCatchupCommand = new EventCatchupCommand();

        final CatchupError indexCatchupError = mock(CatchupError.class);
        final CatchupError eventCatchupError_1 = mock(CatchupError.class);
        final CatchupError eventCatchupError_2 = mock(CatchupError.class);
        final CatchupError eventCatchupError_3 = mock(CatchupError.class);

        catchupErrorStateManager.add(indexCatchupError, new IndexerCatchupCommand());

        assertThat(catchupErrorStateManager.getErrors(eventCatchupCommand), is(emptyList()));

        catchupErrorStateManager.add(eventCatchupError_1, eventCatchupCommand);
        catchupErrorStateManager.add(eventCatchupError_2, eventCatchupCommand);
        catchupErrorStateManager.add(eventCatchupError_3, eventCatchupCommand);

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(eventCatchupCommand);

        assertThat(errors.size(), is(3));
        assertThat(errors.get(0), is(eventCatchupError_1));
        assertThat(errors.get(1), is(eventCatchupError_2));
        assertThat(errors.get(2), is(eventCatchupError_3));

        catchupErrorStateManager.clear(eventCatchupCommand);

        assertThat(catchupErrorStateManager.getErrors(eventCatchupCommand), is(emptyList()));

        assertThat(catchupErrorStateManager.getErrors(new IndexerCatchupCommand()).size(), is(1));
    }

    @Test
    public void shouldKeepListOfAllCatchupErrorsForIndexCatchup() throws Exception {

        final IndexerCatchupCommand indexerCatchupCommand = new IndexerCatchupCommand();

        final CatchupError eventCatchupError = mock(CatchupError.class);
        final CatchupError indexCatchupError_1 = mock(CatchupError.class);
        final CatchupError indexCatchupError_2 = mock(CatchupError.class);
        final CatchupError indexCatchupError_3 = mock(CatchupError.class);

        catchupErrorStateManager.add(eventCatchupError, new EventCatchupCommand());

        assertThat(catchupErrorStateManager.getErrors(indexerCatchupCommand), is(emptyList()));

        catchupErrorStateManager.add(indexCatchupError_1, indexerCatchupCommand);
        catchupErrorStateManager.add(indexCatchupError_2, indexerCatchupCommand);
        catchupErrorStateManager.add(indexCatchupError_3, indexerCatchupCommand);

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(indexerCatchupCommand);

        assertThat(errors.size(), is(3));
        assertThat(errors.get(0), is(indexCatchupError_1));
        assertThat(errors.get(1), is(indexCatchupError_2));
        assertThat(errors.get(2), is(indexCatchupError_3));

        catchupErrorStateManager.clear(indexerCatchupCommand);

        assertThat(catchupErrorStateManager.getErrors(indexerCatchupCommand), is(emptyList()));

        assertThat(catchupErrorStateManager.getErrors(new EventCatchupCommand()).size(), is(1));
    }
}
