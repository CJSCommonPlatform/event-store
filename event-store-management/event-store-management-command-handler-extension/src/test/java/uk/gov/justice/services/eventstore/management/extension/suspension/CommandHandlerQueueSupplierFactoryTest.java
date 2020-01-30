package uk.gov.justice.services.eventstore.management.extension.suspension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.jms.JmsCommandHandlerDestinationNameProvider;
import uk.gov.justice.services.messaging.jms.JmsQueueBrowser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandHandlerQueueSupplierFactoryTest {

    @Mock
    private JmsQueueBrowser jmsQueueBrowser;

    @Mock
    private JmsCommandHandlerDestinationNameProvider jmsCommandHandlerDestinationNameProvider;

    @InjectMocks
    private CommandHandlerQueueSupplierFactory commandHandlerQueueSupplierFactory;

    @Test
    public void shouldReturnTrueWhenCheckingCommandHandlerQueueIsEmptyIfQueueSizeIsZero() {

        final String destination = "destination.name";

        when(jmsCommandHandlerDestinationNameProvider.destinationName()).thenReturn(destination);
        when(jmsQueueBrowser.sizeOf(destination)).thenReturn(0);

        final boolean result = commandHandlerQueueSupplierFactory.isCommandHandlerQueueEmpty().getAsBoolean();

        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnFalseWhenCheckingCommandHandlerQueueIsEmptyIfQueueSizeIsGreaterThanZero() {

        final String destination = "destination.name";

        when(jmsCommandHandlerDestinationNameProvider.destinationName()).thenReturn(destination);
        when(jmsQueueBrowser.sizeOf(destination)).thenReturn(1);

        final boolean result = commandHandlerQueueSupplierFactory.isCommandHandlerQueueEmpty().getAsBoolean();

        assertThat(result, is(false));
    }
}
