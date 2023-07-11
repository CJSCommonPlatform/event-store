package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueue.PublishQueueTableName.PRE_PUBLISH_QUEUE_TABLE;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PrePublishQueueRepositoryTest {

    @Mock
    private PublishQueuesDataAccess publishQueuesDataAccess;

    @InjectMocks
    private PrePublishQueueRepository prePublishQueueRepository;

    @Test
    public void shouldPopNextEventIdFromThePrePublishQueueTable() throws Exception {

        prePublishQueueRepository.popNextEventId();

        verify(publishQueuesDataAccess).popNextEventId(PRE_PUBLISH_QUEUE_TABLE);
    }

    @Test
    public void shouldAddToQueueOfThePrePublishQueueTable() throws Exception {

        final UUID eventId = randomUUID();
        final ZonedDateTime queuedAt = new UtcClock().now();

        prePublishQueueRepository.addToQueue(eventId, queuedAt);

        verify(publishQueuesDataAccess).addToQueue(eventId, queuedAt, PRE_PUBLISH_QUEUE_TABLE);
    }

    @Test
    public void shouldGetSizeOfQueueFromThePrePublishQueueTable() throws Exception {

        final int queueSize = 23;

        when(publishQueuesDataAccess.getSizeOfQueue(PRE_PUBLISH_QUEUE_TABLE)).thenReturn(queueSize);

        assertThat(prePublishQueueRepository.getSizeOfQueue(), is(queueSize));
    }
}