package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventUpdaterTest {

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @Mock
    private  PublishedEventInserter publishedEventInserter;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishedEventUpdater publishedEventUpdater;

    @Test
    public void shouldGetAllEventsAndSaveThem() throws Exception {

        final Set<UUID> activeStreamIds = newHashSet(randomUUID());

        final Event event_1 = mock(Event.class, "event_1");
        final Event event_2 = mock(Event.class, "event_2");
        final Event event_3 = mock(Event.class, "event_3");

        when(activeEventStreamIdProvider.getActiveStreamIds()).thenReturn(activeStreamIds);

        when(eventJdbcRepository.findAllOrderedByEventNumber()).thenReturn(Stream.of(event_1, event_2, event_3));

        when(publishedEventInserter.convertAndSave(eq(event_1), any(AtomicLong.class), eq(activeStreamIds))).thenReturn(1);
        when(publishedEventInserter.convertAndSave(eq(event_2), any(AtomicLong.class), eq(activeStreamIds))).thenReturn(0);
        when(publishedEventInserter.convertAndSave(eq(event_3), any(AtomicLong.class), eq(activeStreamIds))).thenReturn(1);

        publishedEventUpdater.createPublishedEvents();

        final InOrder inOrder = inOrder(
                logger,
                activeEventStreamIdProvider,
                eventJdbcRepository,
                publishedEventInserter
        );

        inOrder.verify(logger).info("Creating PublishedEvents..");
        inOrder.verify(activeEventStreamIdProvider).getActiveStreamIds();
        inOrder.verify(eventJdbcRepository).findAllOrderedByEventNumber();
        inOrder.verify(publishedEventInserter).convertAndSave(eq(event_1), any(AtomicLong.class), eq(activeStreamIds));
        inOrder.verify(publishedEventInserter).convertAndSave(eq(event_2), any(AtomicLong.class), eq(activeStreamIds));
        inOrder.verify(publishedEventInserter).convertAndSave(eq(event_3), any(AtomicLong.class), eq(activeStreamIds));
        inOrder.verify(logger).info("Inserted 2 PublishedEvents");
    }
}
