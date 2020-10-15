package uk.gov.justice.services.test.utils.events;

import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;

public class EventBuilderTest {

    @Test
    public void shouldBuildEvent() throws Exception {

        final UUID eventId = fromString("ca530ced-b905-43b4-ab77-196bb2b8273b");
        final String eventName = "event-name";
        final UUID streamId = fromString("19a4ec31-26f6-470c-857b-a0779807a945");
        final long positionInStream = 23L;
        final String source = "EVENT_LISTENER";
        final ZonedDateTime timestamp = new UtcClock().now();
        final long eventNumber = 982374L;

        final String expectedMetadata =
                "{\"stream\":{" +
                        "\"id\":\"19a4ec31-26f6-470c-857b-a0779807a945\"" +
                        "}," +
                        "\"name\":\"event-name\"," +
                        "\"id\":\"ca530ced-b905-43b4-ab77-196bb2b8273b\"," +
                        "\"source\":\"EVENT_LISTENER\"}";

        final Event event = eventBuilder()
                .withId(eventId)
                .withName(eventName)
                .withStreamId(streamId)
                .withPositionInStream(positionInStream)
                .withSource(source)
                .withTimestamp(timestamp)
                .withEventNumber(eventNumber)
                .build();

        assertThat(event.getId(), is(eventId));
        assertThat(event.getStreamId(), is(streamId));
        assertThat(event.getName(), is(eventName));
        assertThat(event.getPositionInStream(), is(positionInStream));
        assertThat(event.getCreatedAt(), is(timestamp));

        

        assertThat(event.getPayload(), is("{\"field_23\":\"value_23\"}"));
        assertThat(event.getMetadata(), is(expectedMetadata));
    }

    @Test
    public void shouldUseMetadataAndPayloadIfProvided() throws Exception {

        final UUID eventId = fromString("ca530ced-b905-43b4-ab77-196bb2b8273b");
        final String eventName = "event-name";
        final UUID streamId = fromString("19a4ec31-26f6-470c-857b-a0779807a945");
        final long positionInStream = 23L;
        final String source = "EVENT_LISTENER";
        final ZonedDateTime timestamp = new UtcClock().now();
        final long eventNumber = 982374L;

        final String payload = "payload";
        final String metadata = "metadata";

        final Event event = eventBuilder()
                .withId(eventId)
                .withName(eventName)
                .withStreamId(streamId)
                .withPositionInStream(positionInStream)
                .withSource(source)
                .withTimestamp(timestamp)
                .withEventNumber(eventNumber)
                .withPayloadJSON(payload)
                .withMetadataJSON(metadata)
                .build();

        assertThat(event.getId(), is(eventId));
        assertThat(event.getStreamId(), is(streamId));
        assertThat(event.getName(), is(eventName));
        assertThat(event.getPositionInStream(), is(positionInStream));
        assertThat(event.getCreatedAt(), is(timestamp));
        assertThat(event.getEventNumber().orElse(-1L), is(eventNumber));

        assertThat(event.getPayload(), is(payload));
        assertThat(event.getMetadata(), is(metadata));
    }
}
