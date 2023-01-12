package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventConverterTest {

    @Mock
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private PublishedEventConverter publishedEventConverter;

    @Test
    public void shouldConvertEventAndPreviousEventNumberToPublishedEvent() {

        final long eventNumber = 923874L;
        final long previousEventNumber = 923873L;

        final UUID eventId = randomUUID();
        final UUID streamId = randomUUID();
        final long positionInStream = 23487L;
        final String name = "event-name";
        final String payload = "payload";
        final ZonedDateTime createdAt = new UtcClock().now();

        final Metadata metadata = mock(Metadata.class);
        final Metadata updatedMetadata = metadataBuilder()
                .withId(eventId)
                .withName(name)
                .withStreamId(streamId)
                .withEventNumber(eventNumber)
                .withPreviousEventNumber(previousEventNumber)
                .build();

        final Event event = new Event(
                eventId,
                streamId,
                positionInStream,
                name,
                "some metadata",
                payload,
                createdAt,
                Optional.of(eventNumber)
        );

        when(eventConverter.metadataOf(event)).thenReturn(metadata);
        when(metadataEventNumberUpdater.updateMetadataJson(metadata, previousEventNumber, eventNumber)).thenReturn(updatedMetadata);

        final PublishedEvent publishedEvent = publishedEventConverter.toPublishedEvent(event, previousEventNumber);

        assertThat(publishedEvent.getId(), is(eventId));
        assertThat(publishedEvent.getStreamId(), is(streamId));
        assertThat(publishedEvent.getPositionInStream(), is(positionInStream));
        assertThat(publishedEvent.getName(), is(name));
        assertThat(publishedEvent.getPayload(), is(payload));
        assertThat(publishedEvent.getMetadata(), is(updatedMetadata.asJsonObject().toString()));
        assertThat(publishedEvent.getCreatedAt(), is(createdAt));
        assertThat(publishedEvent.getEventNumber().get(), is(eventNumber));
        assertThat(publishedEvent.getPreviousEventNumber(), is(previousEventNumber));
    }

    @Test
    public void shouldThrowExceptionIfEventNumberIsNotPresent() {

        final long previousEventNumber = 923873L;
        final UUID eventId = randomUUID();
        final Event event = mock(Event.class);

        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(Optional.empty());

        try {
            publishedEventConverter.toPublishedEvent(event, previousEventNumber);
            fail();
        } catch (final RebuildException e) {
            assertThat(e.getMessage(), is(format("No event number found for event with id '%s'", eventId)));
        }
    }
}