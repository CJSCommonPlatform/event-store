package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventProcessingFailedHandler;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventQueueConsumer;
import uk.gov.justice.services.event.sourcing.subscription.manager.TransactionalEventProcessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventQueueConsumerFactoryTest {

    @Mock
    private TransactionalEventProcessor transactionalEventProcessor;

    @Mock
    private EventProcessingFailedHandler eventProcessingFailedHandler;

    @InjectMocks
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    @Test
    public void shouldCreateEventQueueConsumerFactory() throws Exception {

        final EventStreamConsumptionResolver eventStreamConsumptionResolver = mock(EventStreamConsumptionResolver.class);

        final EventQueueConsumer eventQueueConsumer = eventQueueConsumerFactory.create(eventStreamConsumptionResolver);

        assertThat(getValueOfField(eventQueueConsumer, "transactionalEventProcessor", TransactionalEventProcessor.class), is(transactionalEventProcessor));
        assertThat(getValueOfField(eventQueueConsumer, "eventStreamConsumptionResolver", EventStreamConsumptionResolver.class), is(eventStreamConsumptionResolver));
        assertThat(getValueOfField(eventQueueConsumer, "eventProcessingFailedHandler", EventProcessingFailedHandler.class), is(eventProcessingFailedHandler));
    }
}
