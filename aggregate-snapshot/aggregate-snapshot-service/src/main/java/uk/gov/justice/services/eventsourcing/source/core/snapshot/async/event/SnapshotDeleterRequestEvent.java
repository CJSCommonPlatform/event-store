package uk.gov.justice.services.eventsourcing.source.core.snapshot.async.event;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.time.ZonedDateTime;
import java.util.StringJoiner;
import java.util.UUID;

public class SnapshotDeleterRequestEvent {
    private final UUID streamId;
    private final Class<? extends Aggregate> aggregateClass;
    private long positionInStream;
    private final ZonedDateTime createdAt;

    public SnapshotDeleterRequestEvent(final UUID uuid, final Class<? extends Aggregate> aggregateClass, final long positionInStream, final ZonedDateTime createdAt) {
        this.streamId = uuid;
        this.aggregateClass = aggregateClass;
        this.positionInStream = positionInStream;
        this.createdAt = createdAt;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Class<? extends Aggregate> getAggregateClass() {
        return aggregateClass;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public long getPositionInStream() {
        return positionInStream;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SnapshotDeleterRequestEvent.class.getSimpleName() + "[", "]")
                .add("streamId=" + getStreamId())
                .add("aggregateClass=" + getAggregateClass())
                .add("positionInStream=" + positionInStream)
                .add("createdAt=" + getCreatedAt())
                .toString();
    }
}
