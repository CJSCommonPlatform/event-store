package uk.gov.justice.services.eventsourcing.publishedevent.publish;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PreviousEventNumberFinder;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.PublishedEventFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MissingEventNumberException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublishedEventProcessorTest {

    @Mock
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PublishedEventFactory publishedEventFactory;

    @Mock
    private PublishedEventRepository publishedEventRepository;

    @Mock
    private PreviousEventNumberFinder previousEventNumberFinder;

    @InjectMocks
    private PublishedEventProcessor publishedEventProcessor;

    @Test
    public void shouldCreatePublishedEvent() throws Exception {

        final UUID eventId = randomUUID();
        final long eventNumber = 10L;
        final long previousEventNumber = 9L;

        final Event event = mock(Event.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final Metadata metadata = mock(Metadata.class, "metadata");
        final Metadata updatedMetadata = mock(Metadata.class, "updatedMetadata");

        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(of(eventNumber));
        when(previousEventNumberFinder.getPreviousEventNumber(eventId, eventNumber)).thenReturn(previousEventNumber);

        when(eventConverter.metadataOf(event)).thenReturn(metadata);
        when(metadataEventNumberUpdater.updateMetadataJson(metadata, previousEventNumber, eventNumber)).thenReturn(updatedMetadata);
        when(publishedEventFactory.create(event, updatedMetadata, eventNumber, previousEventNumber)).thenReturn(publishedEvent);

        publishedEventProcessor.createPublishedEvent(event);

        verify(publishedEventRepository).save(publishedEvent);
    }

    @Test
    public void shouldThrowExceptionIfEventHasNoEventNumber() throws Exception {

        final UUID eventId = fromString("df7958f4-ce50-45a1-a8ee-501d55475e69");

        final Event event = mock(Event.class);

        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(empty());

        try {
            publishedEventProcessor.createPublishedEvent(event);
            fail();
        } catch (final MissingEventNumberException expected) {
            assertThat(expected.getMessage(), is("Event with id 'df7958f4-ce50-45a1-a8ee-501d55475e69' has no event number"));
        }
    }
}
