package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EventStream {

    private final UUID streamId;
    private Long position;
    private boolean active;
    private ZonedDateTime createdAt;

    public EventStream(final UUID streamId,
                       final Long position,
                       final boolean active,
                       final ZonedDateTime createdAt) {
        this.streamId = streamId;
        this.position = position;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
