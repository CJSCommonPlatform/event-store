package uk.gov.justice.services.subscription;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MissingEventRangeStringifierTest {

    @Mock
    private ProcessedEventStreamerConfiguration processedEventStreamerConfiguration;

    @InjectMocks
    private MissingEventRangeStringifier missingEventRangeStringifier;

    @Test
    public void shouldCreatePrettifiedStringOfMissingEventRanges() throws Exception {

        final int maxNumberToLog = 100;
        when(processedEventStreamerConfiguration.getMaxNumberOfMissingEventRangesToLog()).thenReturn(maxNumberToLog);

        final List<MissingEventRange> missingEventRanges = asList(
                new MissingEventRange(4L, 7L),
                new MissingEventRange(8L, 10L)
        );

        final String missingEventRangeString = missingEventRangeStringifier
                .createMissingEventRangeStringFrom(missingEventRanges);

        assertThat(missingEventRangeString, is(
                "Missing Event Ranges: [\n" +
                "MissingEventRange{from event_number: 4 (inclusive) to event_number: 7 (exclusive)},\n" +
                "MissingEventRange{from event_number: 8 (inclusive) to event_number: 10 (exclusive)}\n" +
                "]"
        ));
    }

    @Test
    public void shouldOnlyLogUpToMaximumNumberOfMissingEventRanges() throws Exception {

        final int maxNumberToLog = 3;
        when(processedEventStreamerConfiguration.getMaxNumberOfMissingEventRangesToLog()).thenReturn(maxNumberToLog);

        final List<MissingEventRange> missingEventRanges = asList(
                new MissingEventRange(1L, 9L),
                new MissingEventRange(15L, 23L),
                new MissingEventRange(42L, 57L),
                new MissingEventRange(99L, 146L),
                new MissingEventRange(249L, 287L),
                new MissingEventRange(592L, 306L)
        );

        final String missingEventRangeString = missingEventRangeStringifier
                .createMissingEventRangeStringFrom(missingEventRanges);

        assertThat(missingEventRangeString, is(
                "Missing Event Ranges: [\n" +
                        "MissingEventRange{from event_number: 1 (inclusive) to event_number: 9 (exclusive)},\n" +
                        "MissingEventRange{from event_number: 15 (inclusive) to event_number: 23 (exclusive)},\n" +
                        "MissingEventRange{from event_number: 42 (inclusive) to event_number: 57 (exclusive)}\n" +
                        "<...further messing events not logged...>\n" +
                        "]"
        ));

    }
}