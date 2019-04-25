package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.lang.Math.toIntExact;

import java.util.Objects;
import java.util.UUID;


public class EventBufferEvent implements Comparable<EventBufferEvent> {
    private final UUID streamId;
    private final long position;
    private final String event;
    private final String source;
    private final String component;

    public EventBufferEvent(final UUID streamId, final long position, final String event,
                            final String source, final String component) {
        this.streamId = streamId;
        this.position = position;
        this.event = event;
        this.source = source;
        this.component = component;
    }


    public UUID getStreamId() {
        return streamId;
    }

    public long getPosition() {
        return position;
    }

    public String getEvent() {
        return event;
    }

    public String getSource() {
        return source;
    }


    public String getComponent() {
        return component;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EventBufferEvent that = (EventBufferEvent) o;
        return position == that.position &&
                Objects.equals(streamId, that.streamId) &&
                Objects.equals(event, that.event) &&
                Objects.equals(source, that.source) &&
                Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, position, event, source, component);
    }

    @Override
    public String toString() {
        return "EventBufferEvent{" +
                "streamId=" + streamId +
                ", position=" + position +
                ", event='" + event + '\'' +
                ", source='" + source + '\'' +
                ", component='" + component + '\'' +
                '}';
    }

    @Override
    public int compareTo(final EventBufferEvent eventBufferEvent) {
        return toIntExact(this.getPosition() - eventBufferEvent.getPosition());
    }
}
