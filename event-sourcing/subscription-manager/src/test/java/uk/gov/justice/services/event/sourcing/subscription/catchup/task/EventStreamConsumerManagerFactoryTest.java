package uk.gov.justice.services.event.sourcing.subscription.catchup.task;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventQueueConsumerFactory;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.ConsumeEventQueueBean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventStreamConsumerManagerFactoryTest {

    @Mock
    private ConsumeEventQueueBean consumeEventQueueBean;
    
    @Mock
    private EventStreamsInProgressList eventStreamsInProgressList;

    @Mock
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    @InjectMocks
    private EventStreamConsumerManagerFactory eventStreamConsumerManagerFactory;

    @Test
    public void shouldCreateEventStreamConsumerManager() throws Exception {

        final EventStreamConsumerManager eventStreamConsumerManager = eventStreamConsumerManagerFactory.create();

        assertThat(getValueOfField(eventStreamConsumerManager, "consumeEventQueueBean", ConsumeEventQueueBean.class), is(consumeEventQueueBean));
        assertThat(getValueOfField(eventStreamConsumerManager, "eventStreamsInProgressList", EventStreamsInProgressList.class), is(eventStreamsInProgressList));
        assertThat(getValueOfField(eventStreamConsumerManager, "eventQueueConsumerFactory", EventQueueConsumerFactory.class), is(eventQueueConsumerFactory));
    }
}
