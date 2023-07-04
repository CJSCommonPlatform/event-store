package uk.gov.justice.services.eventstore.management.shuttering.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.polling.MultiIteratingPollerFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueueRepository;
import uk.gov.justice.services.test.utils.common.polling.DummyMultiIteratingPoller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishQueueInterrogatorTest {

    @Mock
    private PublishQueueRepository publishQueueRepository;

    @Mock
    private MultiIteratingPollerFactory multiIteratingPollerFactory;

    @InjectMocks
    private PublishQueueInterrogator publishQueueInterrogator;

    @Test
    public void shouldPollPublishQueueUntilItIsEmpty() throws Exception {

        when(multiIteratingPollerFactory.create(3, 500L, 3, 500L)).thenReturn(new DummyMultiIteratingPoller());
        when(publishQueueRepository.getSizeOfQueue()).thenReturn(0);

        assertThat(publishQueueInterrogator.pollUntilPublishQueueEmpty(), is(true));
    }
}
