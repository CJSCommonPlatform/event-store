package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.eventsourcing.source.core.Tolerance.CONSECUTIVE;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class EnvelopeEventStream implements EventStream {

    private final EventStreamManager eventStreamManager;
    private final UUID id;
    private final LastReadPosition lastReadPosition = new LastReadPosition();
    private long position;
    private String eventSourceName;

    public EnvelopeEventStream(final UUID id, final String eventSourceName, final EventStreamManager eventStreamManager) {

        this.id = id;
        this.eventSourceName = eventSourceName;
        this.eventStreamManager = eventStreamManager;
    }

    public EnvelopeEventStream(final UUID id, final long position, final EventStreamManager eventStreamManager) {

        this.id = id;
        this.eventStreamManager = eventStreamManager;
        this.position = position;
    }

    @Override
    public Stream<JsonEnvelope> read() {

        markAsReadFrom(0L);
        return eventStreamManager.read(id).map(this::recordCurrentPosition);
    }

    @Override
    public Stream<JsonEnvelope> readFrom(final long position) {

        markAsReadFrom(position - 1);
        return eventStreamManager.readFrom(id, position).map(this::recordCurrentPosition);
    }

    @Override
    public Stream<JsonEnvelope> readFrom(final long position, final int pageSize) {

        markAsReadFrom(position - 1);
        return eventStreamManager.readFrom(id, position, pageSize).map(this::recordCurrentPosition);
    }

    @Override
    public long append(final Stream<JsonEnvelope> events) throws EventStreamException {
        return append(events, CONSECUTIVE);
    }

    @Override
    public long append(final Stream<JsonEnvelope> events, final Tolerance tolerance) throws EventStreamException {

        if (tolerance == Tolerance.NON_CONSECUTIVE) {
            return eventStreamManager.appendNonConsecutively(id, events);
        }

        final Optional<Long> readPosition = lastReadPosition.getReadPosition();

        if (readPosition.isPresent()) {
            return eventStreamManager.appendAfter(id, events.map(this::incrementLastReadPosition), readPosition.get());
        }

        return eventStreamManager.append(id, events);
    }

    @Override
    public long appendAfter(final Stream<JsonEnvelope> events, final long position) throws EventStreamException {
        return eventStreamManager.appendAfter(id, events, position);
    }

    @Override
    public long getPosition() {

        if (position == 0) {
            position = eventStreamManager.getStreamPosition(id);
            return position;
        }

        return position;
    }

    @Override
    public long size() {
        return lastReadPosition.getReadPosition().orElseGet(() -> eventStreamManager.getSize(id));
    }

    @Override
    public UUID getId() {
        return id;
    }

    private JsonEnvelope recordCurrentPosition(final JsonEnvelope event) {

        lastReadPosition.setReadPosition(event.metadata().position()
                .orElseThrow(() -> new IllegalStateException("Missing version in event from event store")));

        return event;
    }

    private synchronized JsonEnvelope incrementLastReadPosition(final JsonEnvelope event) {

        lastReadPosition.increment();
        return event;
    }

    private synchronized void markAsReadFrom(final long position) {

        if (lastReadPosition.getReadPosition().isPresent()) {
            throw new IllegalStateException("Event stream has already been read");
        }

        lastReadPosition.setReadPosition(position);
    }

    @Override
    public String getName() {
        return eventSourceName;
    }

    private class LastReadPosition {

        private Long readPosition = null;

        private Optional<Long> getReadPosition() {
            return Optional.ofNullable(readPosition);
        }

        private void setReadPosition(final Long readPosition) {
            this.readPosition = readPosition;
        }

        private void increment() {

            if (null == readPosition) {
                readPosition = 0L;
            }

            readPosition = readPosition + 1;
        }
    }
}
