package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.sourcing.subscription.catchup.EventCatchupConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupStartUpBeanTest {

    @Mock
    private EventCatchupRunner eventCatchupRunner;

    @Mock
    private EventCatchupConfig eventCatchupConfig;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupStartUpBean eventCatchupStartUpBean;

    @Test
    public void shouldRunCatchupIfEnabled() {

        when(eventCatchupConfig.isEventCatchupEnabled()).thenReturn(true);

        eventCatchupStartUpBean.start();

        verify(eventCatchupRunner).runEventCatchup();
        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldNotPerformCatchupIfDisabled() throws Exception {

        when(eventCatchupConfig.isEventCatchupEnabled()).thenReturn(false);

        eventCatchupStartUpBean.start();

        verify(logger).info("Not performing event Event Catchup: Event catchup disabled");
        verifyZeroInteractions(eventCatchupRunner);
    }
}
