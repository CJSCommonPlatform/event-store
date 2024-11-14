package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import uk.gov.justice.services.common.util.UtcClock;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EventBufferEventTest {

    private static final String SOURCE = "source";
    private static final String EVENT_LISTENER = "event_listener";

    @Test
    public void shouldReturnStreamId() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.getStreamId(), is(id));
    }

    @Test
    public void shouldReturnPosition() {
        final UUID id = randomUUID();
        final Long position = 1L;
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, position, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.getPosition(), is(position));
    }

    @Test
    public void shouldReturnEvent() {
        final UUID id = randomUUID();
        final String event = "eventVersion_1";
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, event, SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.getEvent(), is(event));
    }

    @Test
    public void shouldReturnSource() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.getSource(), is(SOURCE));
    }

    @Test
    public void shouldReturnComponent() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.getComponent(), is(EVENT_LISTENER));
    }

    @Test
    public void shouldReturnTrueIfComparingAnObjectToItself() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.equals(eventBufferEvent), is(true));
    }

    @Test
    public void shouldReturnFalseIfComparingAnObjectToNull() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.equals(null), is(false));
    }

    @Test
    public void shouldReturnFalseIfComparingTwoObjectsBelongToDifferentClasses() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);
        final Object object = new Object();
        assertThat(eventBufferEvent.equals(object), is(false));
    }

    @Test
    public void shouldReturnTrueIfAttributeValuesAreIdenticalForTwoObjects() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(true));
    }

    @Test
    public void shouldReturnFalseIfStreamIdsAreNotIdenticalForTwoObjects() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id1, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id2, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(false));
    }

    @Test
    public void shouldReturnFalseIfPositionsAreNotIdenticalForTwoObjects() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id, 2L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(false));
    }

    @Test
    public void shouldReturnFalseIfEventsAreNotIdenticalForTwoObjects() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id, 1L, "eventVersion_2", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(false));
    }

    @Test
    public void shouldReturnFalseIfSourcesAreNotIdenticalForTwoObjects() {
        final UUID id = randomUUID();
        final String source1 = "source1";
        final String source2 = "source2";
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id, 1L, "eventVersion_1", source1, EVENT_LISTENER, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id, 1L, "eventVersion_1", source2, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(false));
    }

    @Test
    public void shouldReturnFalseIfBufferedAtIsNotIdenticalForTwoObjects() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt_1 = new UtcClock().now().minusMinutes(2);
        final ZonedDateTime bufferedAt_2 = bufferedAt_1.plusSeconds(5);
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt_1);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt_2);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(false));
    }

    @Test
    public void shouldReturnFalseIfComponentsAreNotIdenticalForTwoObjects() {
        final UUID id = randomUUID();
        final String component1 = "event_listener1";
        final String component2 = "event_listener2";
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, component1, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, component2, bufferedAt);

        assertThat(eventBufferEvent1.equals(eventBufferEvent2), is(false));
    }

    @Test
    public void shouldReturnHashCode() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.hashCode(), is(notNullValue()));
    }

    @Test
    public void shouldReturnObjectToString() {
        final UUID id = randomUUID();
        final ZonedDateTime bufferedAt = ZonedDateTime.of(2024, 2, 23, 11, 23, 32, 0, UTC);
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent.toString(), is(
                "EventBufferEvent{streamId=" + id + ", position=1, event='eventVersion_1', source='source', component='event_listener', bufferedAt=2024-02-23T11:23:32Z}"));
    }

    @Test
    public void shouldReturnCompareToValue() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();
        final ZonedDateTime bufferedAt = new UtcClock().now();
        final EventBufferEvent eventBufferEvent1 = new EventBufferEvent(id1, 1L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);
        final EventBufferEvent eventBufferEvent2 = new EventBufferEvent(id2, 2L, "eventVersion_1", SOURCE, EVENT_LISTENER, bufferedAt);

        assertThat(eventBufferEvent2.compareTo(eventBufferEvent1), is(1));
    }
}