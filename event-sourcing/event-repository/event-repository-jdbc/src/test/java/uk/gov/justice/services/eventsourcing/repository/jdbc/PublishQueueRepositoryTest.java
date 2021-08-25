package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueue.PublishQueueTableName.PUBLISH_QUEUE_TABLE;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishQueueRepositoryTest {

    @Mock
    private PublishQueuesDataAccess publishQueuesDataAccess;

    @InjectMocks
    private PublishQueueRepository publishQueueRepository;

    @Test
    public void shouldPopNextEventIdFromThePublishQueueTable() throws Exception {

        publishQueueRepository.popNextEventId();

        verify(publishQueuesDataAccess).popNextEventId(PUBLISH_QUEUE_TABLE);
    }

    @Test
    public void shouldAddToQueueOfThePublishQueueTable() throws Exception {

        final UUID eventId = randomUUID();
        final ZonedDateTime queuedAt = new UtcClock().now();

        publishQueueRepository.addToQueue(eventId, queuedAt);

        verify(publishQueuesDataAccess).addToQueue(eventId, queuedAt, PUBLISH_QUEUE_TABLE);
    }

    @Test
    public void shouldGetSizeOfQueueFromThePublishQueueTable() throws Exception {

        final int queueSize = 23;

        when(publishQueuesDataAccess.getSizeOfQueue(PUBLISH_QUEUE_TABLE)).thenReturn(queueSize);

        assertThat(publishQueueRepository.getSizeOfQueue(), is(queueSize));
    }
}