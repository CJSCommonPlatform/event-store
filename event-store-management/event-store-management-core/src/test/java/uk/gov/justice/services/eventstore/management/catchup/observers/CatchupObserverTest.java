package uk.gov.justice.services.eventstore.management.catchup.observers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupCompletedForSubscriptionEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupRequestedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupStartedForSubscriptionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupObserverTest {

    @Mock
    private CatchupLifecycle catchupLifecycle;

    @InjectMocks
    private CatchupObserver catchupObserver;

    @Test
    public void shouldHandleCatchupRequested() throws Exception {

        final CatchupRequestedEvent catchupRequestedEvent = mock(CatchupRequestedEvent.class);

        catchupObserver.onCatchupRequested(catchupRequestedEvent);

        verify(catchupLifecycle).handleCatchupRequested(catchupRequestedEvent);
    }

    @Test
    public void shouldHandleCatchupStartedForSubscription() throws Exception {

        final CatchupStartedForSubscriptionEvent catchupStartedForSubscriptionEvent = mock(CatchupStartedForSubscriptionEvent.class);

        catchupObserver.onCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);

        verify(catchupLifecycle).handleCatchupStartedForSubscription(catchupStartedForSubscriptionEvent);
    }

    @Test
    public void shouldHandleCatchupCompleteForSubscription() throws Exception {

        final CatchupCompletedForSubscriptionEvent catchupCompletedForSubscriptionEvent = mock(CatchupCompletedForSubscriptionEvent.class);

        catchupObserver.onCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);

        verify(catchupLifecycle).handleCatchupCompleteForSubscription(catchupCompletedForSubscriptionEvent);
    }

    @Test
    public void shouldHandleCatchupProcessingOfEventFailed() throws Exception {

        final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent = mock(CatchupProcessingOfEventFailedEvent.class);

        catchupObserver.onCatchupProcessingOfEventFailed(catchupProcessingOfEventFailedEvent);

        verify(catchupLifecycle).handleCatchupProcessingOfEventFailed(catchupProcessingOfEventFailedEvent);
    }
}
