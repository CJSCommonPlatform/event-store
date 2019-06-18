package uk.gov.justice.services.eventstore.management.shuttering.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.polling.MultiIteratingPoller;
import uk.gov.justice.services.common.polling.MultiIteratingPollerFactory;

import java.util.function.BooleanSupplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandHandlerQueueCheckerTest {

    @Mock
    private CommandHandlerQueueSupplierFactory commandHandlerQueueSupplierFactory;

    @Mock
    private MultiIteratingPollerFactory multiIteratingPollerFactory;

    @InjectMocks
    private CommandHandlerQueueChecker commandHandlerQueueChecker;

    @Test
    public void shouldReturnTrueIfHandlerQueueIsEmpty() {

        final int pollerRetryCount = 5;
        final long pollerDelayIntervalMillis = 1000L;
        final int numberOfPollingIterations = 3;
        final long waitTimeBetweenIterationsMillis = 500L;
        final MultiIteratingPoller multiIteratingPoller = mock(MultiIteratingPoller.class);
        final BooleanSupplier booleanSupplier = mock(BooleanSupplier.class);

        when(multiIteratingPollerFactory.create(pollerRetryCount, pollerDelayIntervalMillis, numberOfPollingIterations, waitTimeBetweenIterationsMillis)).thenReturn(multiIteratingPoller);
        when(commandHandlerQueueSupplierFactory.isCommandHandlerQueueEmpty()).thenReturn(booleanSupplier);
        when(multiIteratingPoller.pollUntilTrue(booleanSupplier)).thenReturn(true);

        final boolean state = commandHandlerQueueChecker.pollUntilEmptyHandlerQueue();

        assertThat(state, is(true));
    }
}
