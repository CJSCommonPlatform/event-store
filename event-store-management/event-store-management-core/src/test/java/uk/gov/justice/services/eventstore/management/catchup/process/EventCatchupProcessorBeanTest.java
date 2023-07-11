package uk.gov.justice.services.eventstore.management.catchup.process;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventCatchupProcessorBeanTest {

    @Mock
    private EventCatchupProcessor eventCatchupProcessor;

    @InjectMocks
    private EventCatchupProcessorBean eventCatchupProcessorBean;

    @Test
    public void shouldPerformEventCatchup() throws Exception {

        final CatchupSubscriptionContext catchupSubscriptionContext = mock(CatchupSubscriptionContext.class);

        eventCatchupProcessorBean.performEventCatchup(catchupSubscriptionContext);

        verify(eventCatchupProcessor).performEventCatchup(catchupSubscriptionContext);
    }
}
