package uk.gov.justice.services.eventsourcing.source.core.snapshot.async.event;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.util.StringJoiner;
import java.util.UUID;

public class SnapshotSaverRequestEvent {
    private final UUID streamId;
    private final long positionInStream;
    private final Aggregate aggregate;

    public SnapshotSaverRequestEvent(final UUID uuid, final long positionInStream, final Aggregate aggregate) {
        this.streamId = uuid;
        this.positionInStream = positionInStream;
        this.aggregate = aggregate;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getPositionInStream() {
        return positionInStream;
    }

    public Aggregate getAggregate() {
        return aggregate;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SnapshotSaverRequestEvent.class.getSimpleName() + "[", "]")
                .add("streamId=" + getStreamId())
                .add("positionInStream=" + getPositionInStream())
                .add("aggregate=" + (getAggregate() == null ? "" : getAggregate().getClass().getName()))
                .toString();
    }
}
