package uk.gov.justice.services.eventsourcing.source.api.streams;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class MissingEventRangeToStringTest {

    @Test
    public void shouldCreateHumanReadableStringOfTheMissingEventRange() throws Exception {

        final MissingEventRange missingEventRange = new MissingEventRange(23L, 7632L);

        assertThat(missingEventRange.toString(), is("MissingEventRange{from event_number: 23 (inclusive) to event_number: 7632 (exclusive)}"));
    }
}
