package uk.gov.justice.services.eventstore.management.shuttering.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.polling.MultiIteratingPollerFactory;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.EventDeQueuer;
import uk.gov.justice.services.test.utils.common.polling.DummyMultiIteratingPoller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishQueueInterrogatorTest {

    private static final String PUBLISH_QUEUE_TABLE_NAME = "publish_queue";

    @Mock
    private EventDeQueuer eventDeQueuer;

    @Mock
    private MultiIteratingPollerFactory multiIteratingPollerFactory;
    
    @InjectMocks
    private PublishQueueInterrogator publishQueueInterrogator;

    @Test
    public void shouldPollPublishQueueUntilItIsEmpty() throws Exception {

        when(multiIteratingPollerFactory.create(3, 500L, 3, 500L)).thenReturn(new DummyMultiIteratingPoller());
        when(eventDeQueuer.getSizeOfQueue(PUBLISH_QUEUE_TABLE_NAME)).thenReturn(0);

        assertThat(publishQueueInterrogator.pollUntilPublishQueueEmpty(), is(true));
    }
}
