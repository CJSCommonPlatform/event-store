package uk.gov.justice.services.subscription;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RangeNormalizationCalculatorTest {

    @InjectMocks
    private RangeNormalizationCalculator rangeNormalizationCalculator;

    @Test
    public void shouldSplitLargeRangeToListOfSmallerRangesAccordingToTheMaxRangeSize() throws Exception {

        final int maxRangeSize = 2;
        final MissingEventRange missingEventRange = new MissingEventRange(0L, 11L);

        final List<MissingEventRange> missingEventRanges = rangeNormalizationCalculator
                .asStreamOfNormalizedRanges(missingEventRange, maxRangeSize)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(6));
        assertThat(missingEventRanges.get(0), is(new MissingEventRange(0L, 2L)));
        assertThat(missingEventRanges.get(1), is(new MissingEventRange(2L, 4L)));
        assertThat(missingEventRanges.get(2), is(new MissingEventRange(4L, 6L)));
        assertThat(missingEventRanges.get(3), is(new MissingEventRange(6L, 8L)));
        assertThat(missingEventRanges.get(4), is(new MissingEventRange(8L, 10L)));
        assertThat(missingEventRanges.get(5), is(new MissingEventRange(10L, 11L)));
    }

    @Test
    public void shouldHandleZeroRange() throws Exception {

        final int maxRangeSize = 2;
        final MissingEventRange missingEventRange = new MissingEventRange(0L, 0L);

        final List<MissingEventRange> missingEventRanges = rangeNormalizationCalculator
                .asStreamOfNormalizedRanges(missingEventRange, maxRangeSize)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(1));
        assertThat(missingEventRanges.get(0), is(missingEventRange));
    }

    @Test
    public void shouldHandleRangeOfMaxSize() throws Exception {

        final int maxRangeSize = 2;
        final MissingEventRange missingEventRange = new MissingEventRange(0L, 2L);

        final List<MissingEventRange> missingEventRanges = rangeNormalizationCalculator
                .asStreamOfNormalizedRanges(missingEventRange, maxRangeSize)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(1));
        assertThat(missingEventRanges.get(0), is(missingEventRange));
    }
}