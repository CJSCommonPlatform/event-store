package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultTransactionalEventProcessorTest {

    @Mock
    private CatchupEventBufferProcessor catchupEventBufferProcessor;

    @InjectMocks
    private DefaultTransactionalEventProcessor defaultTransactionalEventProcessor;

    @Test
    public void shouldProcessWithEventBufferAndAlwaysReturnOne() throws Exception {

        final String subscriptionName = "subscriptionName";
        final JsonEnvelope event = mock(JsonEnvelope.class);

        assertThat(defaultTransactionalEventProcessor.processWithEventBuffer(event, subscriptionName), is(1));

        verify(catchupEventBufferProcessor).processWithEventBuffer(event, subscriptionName);
    }
}
