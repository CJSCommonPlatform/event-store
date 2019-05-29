package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventFactoryTest {

    @InjectMocks
    private PublishedEventFactory publishedEventFactory;


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void shouldCreateAPublishedEventFromAnEvent() throws Exception {

        final long eventNumber = 923874L;
        final long previousEventNumber = 923873L;

        final UUID eventId = randomUUID();
        final UUID streamId = randomUUID();
        final long positionInStream = 23487L;
        final String name = "event-name";
        final String payload = "payload";
        final ZonedDateTime createdAt = new UtcClock().now();

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
                createdAt
        );


        final PublishedEvent publishedEvent = publishedEventFactory.create(
                event,
                updatedMetadata,
                eventNumber,
                previousEventNumber);

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
}
