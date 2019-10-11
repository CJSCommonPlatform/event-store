package uk.gov.justice.services.eventstore.management.catchup.observers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;
import uk.gov.justice.services.jmx.logging.MdcLogger;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupObserverTest {

    @Mock
    private CatchupLifecycle catchupLifecycle;

    @Mock
    private MdcLogger mdcLogger;

    @InjectMocks
    private CatchupObserver catchupObserver;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @Test
    public void shouldHandleCatchupRequested() throws Exception {

        final CatchupRequestedEvent catchupRequestedEvent = mock(CatchupRequestedEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupRequested(catchupRequestedEvent);

        verify(catchupLifecycle).handleCatchupRequested(catchupRequestedEvent);
    }

    @Test
    public void shouldHandleCatchupStartedForSubscription() throws Exception {

        final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = mock(CatchupStartedForSubscriptionEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

        verify(catchupLifecycle).handleCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);
    }

    @Test
    public void shouldHandleCatchupCompleteForSubscription() throws Exception {

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = mock(CatchupCompletedForSubscriptionEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(catchupLifecycle).handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);
    }

    @Test
    public void shouldHandleCatchupProcessingOfEventFailed() throws Exception {

        final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent = mock(CatchupProcessingOfEventFailedEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        catchupObserver.onCatchupProcessingOfEventFailed(catchupProcessingOfEventFailedEvent);

        verify(catchupLifecycle).handleCatchupProcessingOfEventFailed(catchupProcessingOfEventFailedEvent);
    }
}
