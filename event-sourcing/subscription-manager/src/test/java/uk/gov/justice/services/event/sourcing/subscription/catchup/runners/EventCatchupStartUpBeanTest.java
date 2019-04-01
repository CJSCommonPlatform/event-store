package uk.gov.justice.services.event.sourcing.subscription.catchup.runners;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.event.sourcing.subscription.catchup.EventCatchupConfig;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupStartUpBeanTest {

    @Mock
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Mock
    private EventCatchupConfig eventCatchupConfig;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventCatchupStartUpBean eventCatchupStartUpBean;

    @Test
    public void shouldRunCatchupIfEnabled() {

        final ZonedDateTime requestedAt = new UtcClock().now();

        when(eventCatchupConfig.isEventCatchupEnabled()).thenReturn(true);
        when(clock.now()).thenReturn(requestedAt);

        eventCatchupStartUpBean.start();

        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(EventCatchupStartUpBean.class.getSimpleName(), requestedAt));
        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldNotPerformCatchupIfDisabled() throws Exception {

        when(eventCatchupConfig.isEventCatchupEnabled()).thenReturn(false);

        eventCatchupStartUpBean.start();

        verify(logger).info("Not performing event Event Catchup: Event catchup disabled");
        verifyZeroInteractions(catchupRequestedEventFirer);
    }
}
