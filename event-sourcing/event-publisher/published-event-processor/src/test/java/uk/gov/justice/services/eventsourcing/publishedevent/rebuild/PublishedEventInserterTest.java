package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventInserterTest {

    @Mock
    private PublishedEventConverter publishedEventConverter;

    @Mock
    private PublishedEventRepository publishedEventRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishedEventInserter publishedEventInserter;

    @Test
    public void shouldConvertToPublishedEventAndSave() throws Exception {

        final UUID streamId = randomUUID();

        final long eventNumber = 24L;
        final AtomicLong previousEventNumber = new AtomicLong(23);
        final Set<UUID> activeStreamIds = newHashSet(streamId);

        final Event event = mock(Event.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(event.getEventNumber()).thenReturn(of(eventNumber));
        when(event.getStreamId()).thenReturn(streamId);
        when(publishedEventConverter.toPublishedEvent(
                event,
                previousEventNumber.get())).thenReturn(publishedEvent);

        assertThat(publishedEventInserter.convertAndSave(event, previousEventNumber, activeStreamIds), is(Optional.of(publishedEvent)));

        verify(publishedEventRepository).save(publishedEvent);
        verifyZeroInteractions(logger);

        assertThat(previousEventNumber.get(), is(eventNumber));
    }

    @Test
    public void shouldDoNothingIfStreamIsInactive() throws Exception {

        final UUID streamId = randomUUID();
        final UUID differentStreamId = randomUUID();

        final long eventNumber = 24L;
        final AtomicLong previousEventNumber = new AtomicLong(23);
        final Set<UUID> activeStreamIds = newHashSet(differentStreamId);

        final Event event = mock(Event.class);

        when(event.getEventNumber()).thenReturn(of(eventNumber));
        when(event.getStreamId()).thenReturn(streamId);

        assertThat(publishedEventInserter.convertAndSave(event, previousEventNumber, activeStreamIds), is(Optional.empty()));

        verifyZeroInteractions(publishedEventConverter);
        verifyZeroInteractions(publishedEventRepository);
        verifyZeroInteractions(logger);

        assertThat(previousEventNumber.get(), is(23L));
    }

    @Test
    public void shouldThrowExceptionIfEventNumberIsMissingFromEvent() throws Exception {

        final UUID eventId = fromString("f8fcae20-8129-4fc7-bee2-bcea1bfe113b");
        final UUID streamId = randomUUID();

        final AtomicLong previousEventNumber = new AtomicLong(23);
        final Set<UUID> activeStreamIds = newHashSet(streamId);

        final Event event = mock(Event.class);

        when(event.getStreamId()).thenReturn(streamId);
        when(event.getEventNumber()).thenReturn(empty());
        when(event.getId()).thenReturn(eventId);

        try {
            publishedEventInserter.convertAndSave(event, previousEventNumber, activeStreamIds);
            fail();
        } catch (final RebuildException expected) {
            assertThat(expected.getMessage(), is("No eventNumber found for event with id 'f8fcae20-8129-4fc7-bee2-bcea1bfe113b'"));
        }

        verifyZeroInteractions(publishedEventConverter);
        verifyZeroInteractions(publishedEventRepository);
        verifyZeroInteractions(logger);
    }
}
