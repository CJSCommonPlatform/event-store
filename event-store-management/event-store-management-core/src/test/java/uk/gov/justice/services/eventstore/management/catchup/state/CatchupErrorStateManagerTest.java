package uk.gov.justice.services.eventstore.management.catchup.state;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupErrorStateManagerTest {


    @InjectMocks
    private CatchupErrorStateManager catchupErrorStateManager;

    @Test
    public void shouldKeepListOfAllCatchupErrorsForEventCatchup() throws Exception {

        final CatchupError indexCatchupError = mock(CatchupError.class);
        final CatchupError eventCatchupError_1 = mock(CatchupError.class);
        final CatchupError eventCatchupError_2 = mock(CatchupError.class);
        final CatchupError eventCatchupError_3 = mock(CatchupError.class);

        catchupErrorStateManager.add(indexCatchupError, INDEX_CATCHUP);

        assertThat(catchupErrorStateManager.getErrors(EVENT_CATCHUP), is(emptyList()));

        catchupErrorStateManager.add(eventCatchupError_1, EVENT_CATCHUP);
        catchupErrorStateManager.add(eventCatchupError_2, EVENT_CATCHUP);
        catchupErrorStateManager.add(eventCatchupError_3, EVENT_CATCHUP);

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(EVENT_CATCHUP);

        assertThat(errors.size(), is(3));
        assertThat(errors.get(0), is(eventCatchupError_1));
        assertThat(errors.get(1), is(eventCatchupError_2));
        assertThat(errors.get(2), is(eventCatchupError_3));

        catchupErrorStateManager.clear(EVENT_CATCHUP);

        assertThat(catchupErrorStateManager.getErrors(EVENT_CATCHUP), is(emptyList()));

        assertThat(catchupErrorStateManager.getErrors(INDEX_CATCHUP).size(), is(1));
    }

    @Test
    public void shouldKeepListOfAllCatchupErrorsForIndexCatchup() throws Exception {

        final CatchupError eventCatchupError = mock(CatchupError.class);
        final CatchupError indexCatchupError_1 = mock(CatchupError.class);
        final CatchupError indexCatchupError_2 = mock(CatchupError.class);
        final CatchupError indexCatchupError_3 = mock(CatchupError.class);

        catchupErrorStateManager.add(eventCatchupError, EVENT_CATCHUP);

        assertThat(catchupErrorStateManager.getErrors(INDEX_CATCHUP), is(emptyList()));

        catchupErrorStateManager.add(indexCatchupError_1, INDEX_CATCHUP);
        catchupErrorStateManager.add(indexCatchupError_2, INDEX_CATCHUP);
        catchupErrorStateManager.add(indexCatchupError_3, INDEX_CATCHUP);

        final List<CatchupError> errors = catchupErrorStateManager.getErrors(INDEX_CATCHUP);

        assertThat(errors.size(), is(3));
        assertThat(errors.get(0), is(indexCatchupError_1));
        assertThat(errors.get(1), is(indexCatchupError_2));
        assertThat(errors.get(2), is(indexCatchupError_3));

        catchupErrorStateManager.clear(INDEX_CATCHUP);

        assertThat(catchupErrorStateManager.getErrors(INDEX_CATCHUP), is(emptyList()));

        assertThat(catchupErrorStateManager.getErrors(EVENT_CATCHUP).size(), is(1));
    }
}
