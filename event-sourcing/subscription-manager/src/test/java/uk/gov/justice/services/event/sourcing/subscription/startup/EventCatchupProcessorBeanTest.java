package uk.gov.justice.services.event.sourcing.subscription.startup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.sourcing.subscription.manager.EventCatchupProcessor;
import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.factories.EventCatchupProcessorFactory;
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

        final String componentName = "componentName";
        final Subscription subscription = mock(Subscription.class);
        final EventCatchupProcessor eventCatchupProcessor = mock(EventCatchupProcessor.class);

        when(eventCatchupProcessorFactory.createFor(componentName)).thenReturn(eventCatchupProcessor);

        eventCatchupProcessorBean.performEventCatchup(componentName, subscription);

        verify(eventCatchupProcessor).performEventCatchup(subscription);
    }
}
