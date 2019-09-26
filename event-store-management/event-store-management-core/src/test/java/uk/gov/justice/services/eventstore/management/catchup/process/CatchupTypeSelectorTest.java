package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupTypeSelectorTest {

    @InjectMocks
    private CatchupTypeSelector catchupTypeSelector;

    @Test
    public void shouldReturnTrueIfTheComponentIsEventListenerAndTheTypeIsEventCatchup() throws Exception {

        assertThat(catchupTypeSelector.isEventCatchup("MY_EVENT_LISTENER", EVENT_CATCHUP), is(true));
        assertThat(catchupTypeSelector.isEventCatchup("MY_EVENT_INDEXER", EVENT_CATCHUP), is(false));
        assertThat(catchupTypeSelector.isEventCatchup("MY_EVENT_PROCESSOR", EVENT_CATCHUP), is(false));
    }

    @Test
    public void shouldReturnTrueIfTheComponentIsEventIndexerAndTheTypeIsIndexCatchup() throws Exception {

        assertThat(catchupTypeSelector.isIndexerCatchup("MY_EVENT_INDEXER", INDEX_CATCHUP), is(true));
        assertThat(catchupTypeSelector.isIndexerCatchup("MY_EVENT_LISTENER", INDEX_CATCHUP), is(false));
        assertThat(catchupTypeSelector.isIndexerCatchup("MY_EVENT_PROCESSOR", INDEX_CATCHUP), is(false));
    }
}
