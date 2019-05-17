package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Optional.empty;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Stream.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventTableCleaner;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventRebuilderTest {

    @Mock
    private EventNumberRenumberer eventNumberRenumberer;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private PublishedEventTableCleaner publishedEventTableCleaner; 

    @Mock
    private PublishedEventConverter publishedEventConverter;

    @Mock
    private PublishedEventRepository publishedEventRepository;

    @Mock
    private ActiveEventStreamIdProvider activeEventStreamIdProvider;

    @InjectMocks
    private PublishedEventRebuilder publishedEventRebuilder;

    @Test
    public void shouldRenumberTheEventLogTableCleanPublishedEventAndFillItWithEvents() throws Exception {

        final UUID streamId = randomUUID();

        final Event event_1 = eventBuilder().withId(randomUUID()).withEventNumber(11).withStreamId(streamId).build();
        final Event event_2 = eventBuilder().withId(randomUUID()).withEventNumber(22).withStreamId(streamId).build();
        final Event event_3 = eventBuilder().withId(randomUUID()).withEventNumber(33).withStreamId(streamId).build();

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        when(activeEventStreamIdProvider.getActiveStreamIds()).thenReturn(newHashSet(streamId));
        when(eventJdbcRepository.findAllOrderedByEventNumber()).thenReturn(of(event_1, event_2, event_3));

        when(publishedEventConverter.toPublishedEvent(event_1, 0L)).thenReturn(publishedEvent_1);
        when(publishedEventConverter.toPublishedEvent(event_2, 11L)).thenReturn(publishedEvent_2);
        when(publishedEventConverter.toPublishedEvent(event_3, 22L)).thenReturn(publishedEvent_3);

        publishedEventRebuilder.rebuild();

        final InOrder inOrder = inOrder(eventNumberRenumberer, publishedEventTableCleaner, publishedEventRepository);

        inOrder.verify(eventNumberRenumberer).renumberEventLogEventNumber();
        inOrder.verify(publishedEventTableCleaner).deleteAll();
        inOrder.verify(publishedEventRepository).save(publishedEvent_1);
        inOrder.verify(publishedEventRepository).save(publishedEvent_2);
        inOrder.verify(publishedEventRepository).save(publishedEvent_3);
    }

    @Test
    public void shouldOnlyAddEventsWithActiveStreams() throws Exception {

        final UUID activeStreamId_1 = randomUUID();
        final UUID activeStreamId_2 = randomUUID();
        final UUID inactiveStreamId = randomUUID();

        final long event_1_eventNumber = 11;
        final long event_2_eventNumber = 22;
        final long event_3_eventNumber = 33;

        final Event event_1 = eventBuilder().withId(randomUUID()).withEventNumber(event_1_eventNumber).withStreamId(activeStreamId_1).build();
        final Event event_2 = eventBuilder().withId(randomUUID()).withEventNumber(event_2_eventNumber).withStreamId(inactiveStreamId).build();
        final Event event_3 = eventBuilder().withId(randomUUID()).withEventNumber(event_3_eventNumber).withStreamId(activeStreamId_2).build();

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class, "published event 1");
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class, "published event 3");

        when(activeEventStreamIdProvider.getActiveStreamIds()).thenReturn(newHashSet(activeStreamId_1, activeStreamId_2));
        when(eventJdbcRepository.findAllOrderedByEventNumber()).thenReturn(of(event_1, event_2, event_3));

        when(publishedEventConverter.toPublishedEvent(event_1, 0L)).thenReturn(publishedEvent_1);
        when(publishedEventConverter.toPublishedEvent(event_3, event_1_eventNumber)).thenReturn(publishedEvent_3);

        publishedEventRebuilder.rebuild();

        verify(publishedEventRepository).save(publishedEvent_1);
        verify(publishedEventRepository).save(publishedEvent_3);

        verify(publishedEventConverter, never()).toPublishedEvent(eq(event_2), anyLong());
    }

    @Test
    public void shouldShouldThrowExceptionIfNoEventNumberFoundForEvent() throws Exception {

        final UUID streamId = randomUUID();

        final Event event_1 = eventBuilder().withStreamId(streamId).withId(randomUUID()).withEventNumber(11).build();
        final Event event_2 = eventBuilder().withStreamId(streamId).withId(fromString("93a5ca5e-6e08-4268-921c-383867355398")).build();

        assertThat(event_2.getEventNumber(), is(empty()));

        final PublishedEvent publishedEvent_1 = mock(PublishedEvent.class);

        when(activeEventStreamIdProvider.getActiveStreamIds()).thenReturn(newHashSet(streamId));
        when(eventJdbcRepository.findAllOrderedByEventNumber()).thenReturn(of(event_1, event_2));
        when(publishedEventConverter.toPublishedEvent(event_1, 0L)).thenReturn(publishedEvent_1);

        try {
            publishedEventRebuilder.rebuild();
            fail();
        } catch (final RebuildException expected) {
            assertThat(expected.getMessage(), is("No eventNumber found for event with id '93a5ca5e-6e08-4268-921c-383867355398'"));
        }
    }
}
