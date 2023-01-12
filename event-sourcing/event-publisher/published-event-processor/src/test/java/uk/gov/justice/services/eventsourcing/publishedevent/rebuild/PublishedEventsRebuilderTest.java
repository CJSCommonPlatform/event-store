package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventsRebuilderTest {

    @Mock
    private EventNumberGetter eventNumberGetter;

    @Mock
    private BatchedPublishedEventInserterFactory batchedPublishedEventInserterFactory;

    @Mock
    private ActiveEventFilter activeEventFilter;

    @Mock
    private RebuildPublishedEventFactory rebuildPublishedEventFactory;

    @InjectMocks
    private PublishedEventsRebuilder publishedEventsRebuilder;

    @Test
    public void shouldConvertEventsToPublishedEventsAndInsertThemInTheDatabaseInBatches() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final Event event_3 = mock(Event.class);

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final Stream<Event> eventStream = Stream.of(event_1, event_2, event_3);
        final AtomicLong currentEventNumber = new AtomicLong(1);
        final AtomicLong previousEventNumber = new AtomicLong(0);
        final Set<UUID> activeStreamIds = newHashSet(streamId_1, streamId_2, streamId_3);

        final BatchedPublishedEventInserter batchedPublishedEventInserter = mock(BatchedPublishedEventInserter.class);

        when(batchedPublishedEventInserterFactory.createInitialised()).thenReturn(batchedPublishedEventInserter);

        when(eventNumberGetter.eventNumberFrom(event_1)).thenReturn(1L);
        when(activeEventFilter.isActiveEvent(event_1, activeStreamIds)).thenReturn(true);
        when(rebuildPublishedEventFactory.createPublishedEventFrom(event_1, previousEventNumber)).thenReturn(publishedEvent_1);
        when(batchedPublishedEventInserter.addToBatch(publishedEvent_1)).thenReturn(publishedEvent_1);

        when(eventNumberGetter.eventNumberFrom(event_2)).thenReturn(2L);
        when(activeEventFilter.isActiveEvent(event_2, activeStreamIds)).thenReturn(true);
        when(rebuildPublishedEventFactory.createPublishedEventFrom(event_2, previousEventNumber)).thenReturn(publishedEvent_2);
        when(batchedPublishedEventInserter.addToBatch(publishedEvent_2)).thenReturn(publishedEvent_2);

        when(eventNumberGetter.eventNumberFrom(event_3)).thenReturn(3L);
        when(activeEventFilter.isActiveEvent(event_3, activeStreamIds)).thenReturn(true);
        when(rebuildPublishedEventFactory.createPublishedEventFrom(event_3, previousEventNumber)).thenReturn(publishedEvent_3);
        when(batchedPublishedEventInserter.addToBatch(publishedEvent_3)).thenReturn(publishedEvent_3);

        final List<PublishedEvent> publishedEvents = publishedEventsRebuilder.rebuild(
                eventStream,
                previousEventNumber, currentEventNumber,
                activeStreamIds);

        assertThat(publishedEvents.size(), is(3));
        assertThat(publishedEvents.get(0), is(publishedEvent_1));
        assertThat(publishedEvents.get(1), is(publishedEvent_2));
        assertThat(publishedEvents.get(2), is(publishedEvent_3));

        final InOrder inOrder = inOrder(batchedPublishedEventInserter);

        inOrder.verify(batchedPublishedEventInserter).addToBatch(publishedEvent_1);
        inOrder.verify(batchedPublishedEventInserter).addToBatch(publishedEvent_2);
        inOrder.verify(batchedPublishedEventInserter).addToBatch(publishedEvent_3);
        inOrder.verify(batchedPublishedEventInserter).insertBatch();
        inOrder.verify(batchedPublishedEventInserter).close();
    }

    @Test
    public void shouldOnlyInsertActiveEvents() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final Event event_3 = mock(Event.class);

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final Stream<Event> eventStream = Stream.of(event_1, event_2, event_3);
        final AtomicLong currentEventNumber = new AtomicLong(1);
        final AtomicLong previousEventNumber = new AtomicLong(0);
        final Set<UUID> activeStreamIds = newHashSet(streamId_1, streamId_2, streamId_3);

        final BatchedPublishedEventInserter batchedPublishedEventInserter = mock(BatchedPublishedEventInserter.class);

        when(batchedPublishedEventInserterFactory.createInitialised()).thenReturn(batchedPublishedEventInserter);

        when(eventNumberGetter.eventNumberFrom(event_1)).thenReturn(1L);
        when(activeEventFilter.isActiveEvent(event_1, activeStreamIds)).thenReturn(true);
        when(rebuildPublishedEventFactory.createPublishedEventFrom(event_1, previousEventNumber)).thenReturn(publishedEvent_1);
        when(batchedPublishedEventInserter.addToBatch(publishedEvent_1)).thenReturn(publishedEvent_1);

        when(eventNumberGetter.eventNumberFrom(event_2)).thenReturn(2L);
        when(activeEventFilter.isActiveEvent(event_2, activeStreamIds)).thenReturn(false);

        when(eventNumberGetter.eventNumberFrom(event_3)).thenReturn(3L);
        when(activeEventFilter.isActiveEvent(event_3, activeStreamIds)).thenReturn(true);
        when(rebuildPublishedEventFactory.createPublishedEventFrom(event_3, previousEventNumber)).thenReturn(publishedEvent_3);
        when(batchedPublishedEventInserter.addToBatch(publishedEvent_3)).thenReturn(publishedEvent_3);

        final List<PublishedEvent> publishedEvents = publishedEventsRebuilder.rebuild(
                eventStream,
                previousEventNumber, currentEventNumber,
                activeStreamIds);

        assertThat(publishedEvents.size(), is(2));
        assertThat(publishedEvents.get(0), is(publishedEvent_1));
        assertThat(publishedEvents.get(1), is(publishedEvent_3));

        final InOrder inOrder = inOrder(batchedPublishedEventInserter);

        inOrder.verify(batchedPublishedEventInserter).addToBatch(publishedEvent_1);
        inOrder.verify(batchedPublishedEventInserter).addToBatch(publishedEvent_3);
        inOrder.verify(batchedPublishedEventInserter).insertBatch();
        inOrder.verify(batchedPublishedEventInserter).close();
    }
}
