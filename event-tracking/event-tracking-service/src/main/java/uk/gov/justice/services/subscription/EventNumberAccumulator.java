package uk.gov.justice.services.subscription;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.LinkedList;

public class EventNumberAccumulator {

    private Long lastPreviousEventNumber = null;
    private Long lastEventNumber = null;

    private final LinkedList<MissingEventRange> missingEventRanges = new LinkedList<>();

    public Long getLastPreviousEventNumber() {
        return lastPreviousEventNumber;
    }

    public void set(final Long lastPreviousEventNumber, final Long lastEventNumber) {
        this.lastPreviousEventNumber = lastPreviousEventNumber;
        this.lastEventNumber = lastEventNumber;
    }

    public boolean isInitialised() {
        return lastEventNumber != null && lastPreviousEventNumber != null;
    }

    public void addRangeFrom(final long currentEventNumber) {
        missingEventRanges.addFirst(new MissingEventRange(
                currentEventNumber + 1,
                lastEventNumber));
    }

    public LinkedList<MissingEventRange> getMissingEventRanges() {
        return missingEventRanges;
    }
}
