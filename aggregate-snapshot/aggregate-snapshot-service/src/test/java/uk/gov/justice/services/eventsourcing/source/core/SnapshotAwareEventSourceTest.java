package uk.gov.justice.services.eventsourcing.source.core;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.DefaultEventStreamMetadata;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventStreamMetadata;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcBasedEventRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SnapshotAwareEventSourceTest {

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private JdbcBasedEventRepository eventRepository;

    @InjectMocks
    private SnapshotAwareEventSource snapshotAwareEventSource;

    @Test
    public void shouldGetEventStreamById() {
        final EnvelopeEventStream eventStream = (EnvelopeEventStream) snapshotAwareEventSource.getStreamById(STREAM_ID);

        assertThat(eventStream.getId(), equalTo(STREAM_ID));
    }

    @Test
    public void shouldGetAStreamOfEventStreams() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();

        final long position_1 = 23;
        final long position_2 = 24;

        final EventStreamMetadata eventStreamMetadata_1 = mock(EventStreamMetadata.class);
        final EventStreamMetadata eventStreamMetadata_2 = mock(EventStreamMetadata.class);

        when(eventStreamMetadata_1.getStreamId()).thenReturn(streamId_1);
        when(eventStreamMetadata_1.getPosition()).thenReturn(position_1);
        when(eventStreamMetadata_2.getStreamId()).thenReturn(streamId_2);
        when(eventStreamMetadata_2.getPosition()).thenReturn(position_2);

        when(eventRepository.getStreams()).thenReturn(Stream.of(eventStreamMetadata_1, eventStreamMetadata_2));

        final List<EventStream> eventStreams = snapshotAwareEventSource.getStreams().collect(toList());

        assertThat(eventStreams.size(), is(2));

        assertThat(eventStreams.get(0).getId(), is(streamId_1));
        assertThat(eventStreams.get(0).getPosition(), is(position_1));

        assertThat(eventStreams.get(1).getId(), is(streamId_2));
        assertThat(eventStreams.get(1).getPosition(), is(position_2));
    }

    @Test
    public void shouldGetEventStreamsFromPosition() {
        final UUID streamId = randomUUID();
        final long position = 1L;

        final Stream<EventStreamMetadata> eventStreamMetadatas = Stream.of(new DefaultEventStreamMetadata(streamId, position, true, now()));
        when(eventRepository.getEventStreamsFromPosition(position)).thenReturn(eventStreamMetadatas);

        final Stream<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreams = snapshotAwareEventSource.getStreamsFrom(position);
        List<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreamList = eventStreams.collect(toList());

        assertThat(eventStreamList.size(), is(1));
        assertThat(eventStreamList.get(0), instanceOf(uk.gov.justice.services.eventsourcing.source.core.EventStream.class));
        assertThat(eventStreamList.get(0), instanceOf(EnvelopeEventStream.class));
        assertThat(eventStreamList.get(0).getId(), is(streamId));
        assertThat(eventStreamList.get(0).getPosition(), is(position));
    }

    @Test
    public void shouldReturnEmptyStream() {
        final long sequenceNumber = 9L;

        when(eventRepository.getEventStreamsFromPosition(sequenceNumber)).thenReturn(Stream.empty());

        final Stream<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreams = snapshotAwareEventSource.getStreamsFrom(sequenceNumber);
        List<uk.gov.justice.services.eventsourcing.source.core.EventStream> eventStreamList = eventStreams.collect(toList());

        assertThat(eventStreamList.size(), is(0));
    }
}
