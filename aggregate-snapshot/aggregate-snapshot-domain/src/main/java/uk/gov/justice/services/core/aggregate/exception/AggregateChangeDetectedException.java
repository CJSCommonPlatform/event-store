package uk.gov.justice.services.core.aggregate.exception;

import java.time.ZonedDateTime;

public class AggregateChangeDetectedException extends Exception {

    private static final long serialVersionUID = 5934757852541650746L;

    private final long positionInStream;
    private final ZonedDateTime createdAt;

    public AggregateChangeDetectedException(final String message, final long positionInStream, final ZonedDateTime createdAt) {
        super(message);
        this.positionInStream = positionInStream;
        this.createdAt = createdAt;
    }

    public long getPositionInStream() {
        return positionInStream;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
