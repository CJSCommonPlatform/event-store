package uk.gov.justice.services.test.utils.events;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.events.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;

public class PublishedEventBuilderTest {

    @Test
    public void shouldBuildPublishedEvent() throws Exception {

        final UUID eventId = fromString("ca530ced-b905-43b4-ab77-196bb2b8273b");
        final String eventName = "event-name";
        final UUID streamId = fromString("19a4ec31-26f6-470c-857b-a0779807a945");
        final long positionInStream = 23L;
        final String source = "EVENT_LISTENER";
        final ZonedDateTime timestamp = new UtcClock().now();
        final long previousEventNumber = 23;
        final long eventNumber = 24;

        final String expectedMetadata =
                "{\"stream\":{" +
                        "\"id\":\"19a4ec31-26f6-470c-857b-a0779807a945\"" +
                        "}," +
                        "\"name\":\"event-name\"," +
                        "\"id\":\"ca530ced-b905-43b4-ab77-196bb2b8273b\"," +
                        "\"source\":\"EVENT_LISTENER\"}";

        final PublishedEvent publishedEvent = publishedEventBuilder()
                .withId(eventId)
                .withName(eventName)
                .withStreamId(streamId)
                .withPositionInStream(positionInStream)
                .withSource(source)
                .withTimestamp(timestamp)
                .withEventNumber(eventNumber)
                .withPreviousEventNumber(previousEventNumber)
                .build();

        assertThat(publishedEvent.getId(), is(eventId));
        assertThat(publishedEvent.getStreamId(), is(streamId));
        assertThat(publishedEvent.getName(), is(eventName));
        assertThat(publishedEvent.getPositionInStream(), is(positionInStream));
        assertThat(publishedEvent.getCreatedAt(), is(timestamp));
        assertThat(publishedEvent.getEventNumber().orElse(-1L), is(eventNumber));
        assertThat(publishedEvent.getPreviousEventNumber(), is(previousEventNumber));

        assertThat(publishedEvent.getPayload(), is("{\"field_23\":\"value_23\"}"));
        assertThat(publishedEvent.getMetadata(), is(expectedMetadata));
    }

    @Test
    public void shouldUseMetadataAndPayloadIfProvided() throws Exception {

        final UUID eventId = fromString("ca530ced-b905-43b4-ab77-196bb2b8273b");
        final String eventName = "event-name";
        final UUID streamId = fromString("19a4ec31-26f6-470c-857b-a0779807a945");
        final long positionInStream = 23L;
        final String source = "EVENT_LISTENER";
        final ZonedDateTime timestamp = new UtcClock().now();
        final long previousEventNumber = 23;
        final long eventNumber = 24;

        final String payload = "payload";
        final String metadata = "metadata";

        final PublishedEvent publishedEvent = publishedEventBuilder()
                .withId(eventId)
                .withName(eventName)
                .withStreamId(streamId)
                .withPositionInStream(positionInStream)
                .withSource(source)
                .withTimestamp(timestamp)
                .withPreviousEventNumber(previousEventNumber)
                .withEventNumber(eventNumber)
                .withPayloadJSON(payload)
                .withMetadataJSON(metadata)
                .build();

        assertThat(publishedEvent.getId(), is(eventId));
        assertThat(publishedEvent.getStreamId(), is(streamId));
        assertThat(publishedEvent.getName(), is(eventName));
        assertThat(publishedEvent.getPositionInStream(), is(positionInStream));
        assertThat(publishedEvent.getCreatedAt(), is(timestamp));
        assertThat(publishedEvent.getEventNumber().orElse(-1L), is(eventNumber));
        assertThat(publishedEvent.getPreviousEventNumber(), is(previousEventNumber));

        assertThat(publishedEvent.getPayload(), is(payload));
        assertThat(publishedEvent.getMetadata(), is(metadata));
    }}
