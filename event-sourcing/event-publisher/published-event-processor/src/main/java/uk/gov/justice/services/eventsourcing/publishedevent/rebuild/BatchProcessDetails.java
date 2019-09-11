package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class BatchProcessDetails {

    private final AtomicLong previousEventNumber;
    private final AtomicLong currentEventNumber;
    private final int processCount;
    private final boolean complete;

    public BatchProcessDetails(final AtomicLong previousEventNumber, final AtomicLong currentEventNumber, final int processCount, final boolean complete) {
        this.previousEventNumber = previousEventNumber;
        this.currentEventNumber = currentEventNumber;
        this.processCount = processCount;
        this.complete = complete;
    }

    public AtomicLong getPreviousEventNumber() {
        return previousEventNumber;
    }

    public AtomicLong getCurrentEventNumber() {
        return currentEventNumber;
    }

    public int getProcessCount() {
        return processCount;
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BatchProcessDetails)) return false;
        final BatchProcessDetails that = (BatchProcessDetails) o;
        return processCount == that.processCount &&
                complete == that.complete &&
                Objects.equals(previousEventNumber, that.previousEventNumber) &&
                Objects.equals(currentEventNumber, that.currentEventNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previousEventNumber, currentEventNumber, processCount, complete);
    }

    @Override
    public String toString() {
        return "BatchProcessDetails{" +
                "previousEventNumber=" + previousEventNumber +
                ", currentEventNumber=" + currentEventNumber +
                ", processCount=" + processCount +
                ", complete=" + complete +
                '}';
    }
}
