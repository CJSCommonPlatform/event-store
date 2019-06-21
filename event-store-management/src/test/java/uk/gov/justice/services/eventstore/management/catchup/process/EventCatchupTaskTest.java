package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class EventCatchupTaskTest {

    @Test
    public void shouldStartSubscription() throws Exception {

        final CatchupContext catchupContext = mock(CatchupContext.class);
        final EventCatchupProcessorBean eventCatchupProcessorBean = mock(EventCatchupProcessorBean.class);

        final EventCatchupTask eventCatchupTask = new EventCatchupTask(catchupContext, eventCatchupProcessorBean);

        assertThat(eventCatchupTask.call(), is(true));

        verify(eventCatchupProcessorBean).performEventCatchup(catchupContext);
    }
}
