package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ActiveEventStreamIdProviderTest {

    @Mock
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @InjectMocks
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @Test
    public void shouldReturnAListOfIdsOfAllTheActiveStreams() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();
        final UUID streamId_4 = randomUUID();

        final ZonedDateTime now = new UtcClock().now();

        final AtomicBoolean streamClosed = new AtomicBoolean(false);

        final Stream<EventStream> eventStreamStream = Stream.of(
                new EventStream(streamId_1, 1L, true, now),
                new EventStream(streamId_2, 2L, true, now),
                new EventStream(streamId_3, 3L, true, now),
                new EventStream(streamId_4, 4L, true, now)).onClose(() -> streamClosed.set(true));

        when(eventStreamJdbcRepository.findActive()).thenReturn(eventStreamStream);

        final Set<UUID> activeStreamIds = activeEventStreamIdProvider.getActiveStreamIds();

        assertThat(activeStreamIds.size(), is(4));

        assertThat(activeStreamIds, hasItem(streamId_1));
        assertThat(activeStreamIds, hasItem(streamId_2));
        assertThat(activeStreamIds, hasItem(streamId_3));
        assertThat(activeStreamIds, hasItem(streamId_4));

        assertThat(streamClosed.get(), is(true));
    }
}
