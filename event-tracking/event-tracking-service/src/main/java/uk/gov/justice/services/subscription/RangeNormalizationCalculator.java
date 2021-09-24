package uk.gov.justice.services.subscription;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RangeNormalizationCalculator {

    public Stream<MissingEventRange> asStreamOfNormalizedRanges(final MissingEventRange missingEventRange, final long maxRangeSize) {
        final Long from = missingEventRange.getMissingEventFrom();
        final Long to = missingEventRange.getMissingEventTo();

        final List<MissingEventRange> normalizedEventRanges = new ArrayList<>();

        long currentRange = to - from;
        if (currentRange > maxRangeSize) {

            long currentFrom = from;
            while (currentRange > 0) {

                long newTo = currentFrom + maxRangeSize;

                if(newTo > to) {
                    newTo = to;
                }

                normalizedEventRanges.add(new MissingEventRange(currentFrom, newTo));
                currentRange -= maxRangeSize;
                currentFrom = newTo;
            }
        }  else {
            normalizedEventRanges.add(missingEventRange);
        }

        return normalizedEventRanges.stream();
    }
}
