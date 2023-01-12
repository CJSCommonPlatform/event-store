package uk.gov.justice.services.subscription;

import static java.util.Arrays.asList;
import static java.util.stream.Stream.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventRangeNormalizerTest {

    @Mock
    private RangeNormalizationCalculator rangeNormalizationCalculator;

    @Mock
    private PublishedEventReadConfiguration publishedEventReadConfiguration;
    
    @InjectMocks
    private EventRangeNormalizer eventRangeNormalizer;

    @Test
    public void shouldBreakLargeRangesIntoSmallerOnesWithMaximumSize() throws Exception {

        final Long rangeNormalizationMaxSize = 10L;

        final MissingEventRange missingEventRange_1 = mock(MissingEventRange.class);
        final MissingEventRange missingEventRange_2 = mock(MissingEventRange.class);
        final MissingEventRange missingEventRange_3 = mock(MissingEventRange.class);

        final MissingEventRange normalisedRange_1 = mock(MissingEventRange.class);
        final MissingEventRange normalisedRange_2 = mock(MissingEventRange.class);
        final MissingEventRange normalisedRange_3 = mock(MissingEventRange.class);
        final MissingEventRange normalisedRange_4 = mock(MissingEventRange.class);
        final MissingEventRange normalisedRange_5 = mock(MissingEventRange.class);
        final MissingEventRange normalisedRange_6 = mock(MissingEventRange.class);

        final Stream<MissingEventRange> normalisedRanges_1 = of(
                normalisedRange_1,
                normalisedRange_2);
        final Stream<MissingEventRange> normalisedRanges_2 = of(
                normalisedRange_3,
                normalisedRange_4);
        final Stream<MissingEventRange> normalisedRanges_3 = of(
                normalisedRange_5,
                normalisedRange_6);

        when(publishedEventReadConfiguration.getRangeNormalizationMaxSize()).thenReturn(rangeNormalizationMaxSize);
        when(rangeNormalizationCalculator.asStreamOfNormalizedRanges(missingEventRange_1, rangeNormalizationMaxSize))
                .thenReturn(normalisedRanges_1);
        when(rangeNormalizationCalculator.asStreamOfNormalizedRanges(missingEventRange_2, rangeNormalizationMaxSize))
                .thenReturn(normalisedRanges_2);
        when(rangeNormalizationCalculator.asStreamOfNormalizedRanges(missingEventRange_3, rangeNormalizationMaxSize))
                .thenReturn(normalisedRanges_3);

        final List<MissingEventRange> allNormalisedRanges = eventRangeNormalizer.normalize(new LinkedList<>(asList(
                missingEventRange_1,
                missingEventRange_2,
                missingEventRange_3)));

        assertThat(allNormalisedRanges.size(), is(6));

        assertThat(allNormalisedRanges.get(0), is(normalisedRange_1));
        assertThat(allNormalisedRanges.get(1), is(normalisedRange_2));
        assertThat(allNormalisedRanges.get(2), is(normalisedRange_3));
        assertThat(allNormalisedRanges.get(3), is(normalisedRange_4));
        assertThat(allNormalisedRanges.get(4), is(normalisedRange_5));
        assertThat(allNormalisedRanges.get(5), is(normalisedRange_6));
    }
}