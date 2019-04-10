package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupProcessorBeanTest {

    @Mock
    private EventCatchupProcessorFactory eventCatchupProcessorFactory;

    @InjectMocks
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Test
    public void shouldPerformEventCatchup() throws Exception {

        final Subscription subscription = mock(Subscription.class);
        final EventCatchupProcessor eventCatchupProcessor = mock(EventCatchupProcessor.class);

        when(eventCatchupProcessorFactory.create()).thenReturn(eventCatchupProcessor);

        eventCatchupProcessorBean.performEventCatchup(subscription);

        verify(eventCatchupProcessor).performEventCatchup(subscription);
    }
}
