package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamServiceTest {

    @Mock
    private EventSource eventSource;

    @InjectMocks
    private EventStreamService service;

    @Test
    public void shouldReturnHeadEvents() throws Exception {

        final long pageSize = 2L;

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();
        final UUID streamId_4 = randomUUID();

        final EventStream eventStream_1 = mock(EventStream.class);
        final EventStream eventStream_2 = mock(EventStream.class);
        final EventStream eventStream_3 = mock(EventStream.class);
        final EventStream eventStream_4 = mock(EventStream.class);

        when(eventStream_3.getId()).thenReturn(streamId_3);
        when(eventStream_4.getId()).thenReturn(streamId_4);

        when(eventStream_3.getPosition()).thenReturn(3L);
        when(eventStream_4.getPosition()).thenReturn(4L);

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        eventStreamBuilder.add(eventStream_1);
        eventStreamBuilder.add(eventStream_2);
        eventStreamBuilder.add(eventStream_3);
        eventStreamBuilder.add(eventStream_4);

        final Stream.Builder<EventStream> eventStreamBuilderFrom3 = Stream.builder();
        eventStreamBuilderFrom3.add(eventStream_3);
        eventStreamBuilderFrom3.add(eventStream_4);

        when(eventSource.getStreamsFrom(1)).thenReturn(eventStreamBuilder.build());
        when(eventSource.getStreamsFrom(3)).thenReturn(eventStreamBuilderFrom3.build());

        final List<EventStreamEntry> entries = service.eventStreams(head(), BACKWARD, pageSize);

        assertThat(entries, hasSize(2));

        assertThat(entries.get(0).getStreamId(), is(streamId_3.toString()));
        assertThat(entries.get(0).getSequenceNumber(), is(3L));

        assertThat(entries.get(1).getStreamId(), is(streamId_4.toString()));
        assertThat(entries.get(1).getSequenceNumber(), is(4L));
    }

    @Test
    public void shouldReturnFirstEvents() throws Exception {
        final long pageSize = 2L;

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();

        final EventStream eventStream_1 = mock(EventStream.class);
        final EventStream eventStream_2 = mock(EventStream.class);

        when(eventStream_1.getId()).thenReturn(streamId_1);
        when(eventStream_2.getId()).thenReturn(streamId_2);

        when(eventStream_1.getPosition()).thenReturn(1L);
        when(eventStream_2.getPosition()).thenReturn(2L);

        eventStreamBuilder.add(eventStream_1);
        eventStreamBuilder.add(eventStream_2);

        final Stream.Builder<EventStream> eventStreamBuilderFrom1 = Stream.builder();
        eventStreamBuilderFrom1.add(eventStream_1);
        eventStreamBuilderFrom1.add(eventStream_2);

        when(eventSource.getStreamsFrom(1)).thenReturn(eventStreamBuilder.build());

        final List<EventStreamEntry> eventStreamEntries = service.eventStreams(first(), FORWARD, pageSize);

        assertThat(eventStreamEntries, hasSize(2));

        assertThat(eventStreamEntries.get(0).getStreamId(), is(streamId_1.toString()));
        assertThat(eventStreamEntries.get(0).getSequenceNumber(), is(1L));

        assertThat(eventStreamEntries.get(1).getStreamId(), is(streamId_2.toString()));
        assertThat(eventStreamEntries.get(1).getSequenceNumber(), is(2L));
    }

    @Test
    public void shouldReturnPreviousEvents() throws Exception {

        final long pageSize = 2L;

        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final EventStream eventStream_2 = mock(EventStream.class);
        final EventStream eventStream_3 = mock(EventStream.class);

        when(eventStream_2.getId()).thenReturn(streamId_2);
        when(eventStream_3.getId()).thenReturn(streamId_3);

        when(eventStream_2.getPosition()).thenReturn(2L);
        when(eventStream_3.getPosition()).thenReturn(3L);

        eventStreamBuilder.add(eventStream_2);
        eventStreamBuilder.add(eventStream_3);

        final long position = 3L;

        when(eventSource.getStreamsFrom(2L)).thenReturn(eventStreamBuilder.build());

        final List<EventStreamEntry> eventEntries = service.eventStreams(position(position), BACKWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId_2.toString()));
        assertThat(eventEntries.get(0).getSequenceNumber(), is(2L));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId_3.toString()));
        assertThat(eventEntries.get(1).getSequenceNumber(), is(3L));
    }

    @Test
    public void shouldReturnNextEvents() throws Exception {

        final long pageSize = 2L;

        final UUID streamId_5 = randomUUID();
        final UUID streamId_4 = randomUUID();

        final Stream.Builder<EventStream> eventStreamBuilder = Stream.builder();

        final EventStream eventStream_4 = mock(EventStream.class);
        final EventStream eventStream_5 = mock(EventStream.class);

        when(eventStream_4.getId()).thenReturn(streamId_4);
        when(eventStream_5.getId()).thenReturn(streamId_5);

        when(eventStream_4.getPosition()).thenReturn(4L);
        when(eventStream_5.getPosition()).thenReturn(5L);

        eventStreamBuilder.add(eventStream_4);
        eventStreamBuilder.add(eventStream_5);

        final long position = 3L;

        when(eventSource.getStreamsFrom(position)).thenReturn(eventStreamBuilder.build());

        final List<EventStreamEntry> eventEntries = service.eventStreams(position(position), FORWARD, pageSize);

        assertThat(eventEntries, hasSize(2));

        assertThat(eventEntries.get(0).getStreamId(), is(streamId_4.toString()));
        assertThat(eventEntries.get(0).getSequenceNumber(), is(4L));

        assertThat(eventEntries.get(1).getStreamId(), is(streamId_5.toString()));
        assertThat(eventEntries.get(1).getSequenceNumber(), is(5L));
    }
}
