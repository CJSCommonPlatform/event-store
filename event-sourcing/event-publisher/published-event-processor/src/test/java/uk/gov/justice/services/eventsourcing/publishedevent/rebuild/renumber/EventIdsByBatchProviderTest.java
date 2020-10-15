package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventIdsByBatchProviderTest {

    @InjectMocks
    private EventIdsByBatchProvider eventIdsByBatchProvider;

    @Test
    public void shouldGetBatchOfIdsByBatchSize() throws Exception {

        final int batchSize = 3;

        final ResultSet resultSet = mock(ResultSet.class);
        final UUID eventId_1 = randomUUID();
        final UUID eventId_2 = randomUUID();
        final UUID eventId_3 = randomUUID();

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(eventId_1, eventId_2, eventId_3);

        final EventIdBatch nextBatchOfIds = eventIdsByBatchProvider.getNextBatchOfIds(resultSet, batchSize);

        assertThat(nextBatchOfIds.getEventIds().size(), is(3));
        assertThat(nextBatchOfIds.getEventIds().get(0), is(eventId_1));
        assertThat(nextBatchOfIds.getEventIds().get(1), is(eventId_2));
        assertThat(nextBatchOfIds.getEventIds().get(2), is(eventId_3));
    }

    @Test
    public void shouldHandleLessIdsRemainingThanTheBatchSize() throws Exception {

        final int batchSize = 10;

        final ResultSet resultSet = mock(ResultSet.class);
        final UUID eventId_1 = randomUUID();
        final UUID eventId_2 = randomUUID();
        final UUID eventId_3 = randomUUID();

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn(eventId_1, eventId_2, eventId_3);

        final EventIdBatch nextBatchOfIds = eventIdsByBatchProvider.getNextBatchOfIds(resultSet, batchSize);

        assertThat(nextBatchOfIds.getEventIds().size(), is(3));
        assertThat(nextBatchOfIds.getEventIds().get(0), is(eventId_1));
        assertThat(nextBatchOfIds.getEventIds().get(1), is(eventId_2));
        assertThat(nextBatchOfIds.getEventIds().get(2), is(eventId_3));
    }
}
