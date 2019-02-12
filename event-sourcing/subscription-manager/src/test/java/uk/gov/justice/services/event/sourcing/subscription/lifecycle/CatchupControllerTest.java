package uk.gov.justice.services.event.sourcing.subscription.lifecycle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupControllerTest {

    @InjectMocks
    private CatchupController catchupController;

    @Test
    public void shouldCallCatchupStartedOnAllCatchupListeners() throws Exception {

        final ZonedDateTime catchupStartedTime_1 = new UtcClock().now();
        final ZonedDateTime catchupStartedTime_2 = catchupStartedTime_1.plusMinutes(23);

        final CatchupProcessListener catchupProcessListener_1 = mock(CatchupProcessListener.class);
        final CatchupProcessListener catchupProcessListener_2 = mock(CatchupProcessListener.class);

        catchupController.addCatchupProcessListener(catchupProcessListener_1);
        catchupController.addCatchupProcessListener(catchupProcessListener_2);

        final CatchupStartedEvent catchupStartedEvent_1 = new CatchupStartedEvent(catchupStartedTime_1);
        final CatchupStartedEvent catchupStartedEvent_2 = new CatchupStartedEvent(catchupStartedTime_2);

        catchupController.fireCatchupStarted(catchupStartedEvent_1);

        verify(catchupProcessListener_1).onCatchupStarted(catchupStartedEvent_1);
        verify(catchupProcessListener_2).onCatchupStarted(catchupStartedEvent_1);

        catchupController.removeCatchupProcessListener(catchupProcessListener_2);

        catchupController.fireCatchupStarted(catchupStartedEvent_2);

        verify(catchupProcessListener_1).onCatchupStarted(catchupStartedEvent_2);
        verify(catchupProcessListener_2, never()).onCatchupStarted(catchupStartedEvent_2);
    }

    @Test
    public void shouldCallCatchupCompletedOnAllCatchupListeners() throws Exception {

        final long currentEventNumber_1 = 23498L;
        final int totalNumberOfEvents_1 = 23;

        final long currentEventNumber_2 = 98273L;
        final int totalNumberOfEvents_2 = 83;

        final ZonedDateTime catchupCompletedTime_1 = new UtcClock().now();
        final ZonedDateTime catchupCompletedTime_2 = catchupCompletedTime_1.plusMinutes(23);

        final CatchupCompletedEvent catchupCompletedEvent_1 = new CatchupCompletedEvent(
                currentEventNumber_1,
                totalNumberOfEvents_1,
                catchupCompletedTime_1);
        final CatchupCompletedEvent catchupCompletedEvent_2 = new CatchupCompletedEvent(
                currentEventNumber_2,
                totalNumberOfEvents_2,
                catchupCompletedTime_2);

        final CatchupProcessListener catchupProcessListener_1 = mock(CatchupProcessListener.class);
        final CatchupProcessListener catchupProcessListener_2 = mock(CatchupProcessListener.class);

        catchupController.addCatchupProcessListener(catchupProcessListener_1);
        catchupController.addCatchupProcessListener(catchupProcessListener_2);

        catchupController.fireCatchupCompleted(catchupCompletedEvent_1);

        verify(catchupProcessListener_1).onCatchupCompleted(catchupCompletedEvent_1);
        verify(catchupProcessListener_2).onCatchupCompleted(catchupCompletedEvent_1);

        catchupController.removeCatchupProcessListener(catchupProcessListener_2);

        catchupController.fireCatchupCompleted(catchupCompletedEvent_2);

        verify(catchupProcessListener_1).onCatchupCompleted(catchupCompletedEvent_2);
        verify(catchupProcessListener_2, never()).onCatchupCompleted(catchupCompletedEvent_2);
    }
}
