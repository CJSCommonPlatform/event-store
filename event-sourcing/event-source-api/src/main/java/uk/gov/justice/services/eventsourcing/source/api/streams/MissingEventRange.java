package uk.gov.justice.services.eventsourcing.source.api.streams;

import java.util.Objects;

public class MissingEventRange {

    private final Long missingEventFrom;
    private final Long missingEventTo;

    public MissingEventRange(final Long missingEventFrom, final Long missingEventTo) {
        this.missingEventFrom = missingEventFrom;
        this.missingEventTo = missingEventTo;
    }

    public Long getMissingEventFrom() {
        return missingEventFrom;
    }

    public Long getMissingEventTo() {
        return missingEventTo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MissingEventRange)) return false;
        final MissingEventRange that = (MissingEventRange) o;
        return Objects.equals(missingEventFrom, that.missingEventFrom) &&
                Objects.equals(missingEventTo, that.missingEventTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missingEventFrom, missingEventTo);
    }

    @Override
    public String toString() {
        return "MissingEventRange{" +
                "missingEventFrom (inclusive) = " + missingEventFrom +
                ", missingEventTo (exclusive) = " + missingEventTo +
                '}';
    }
}
