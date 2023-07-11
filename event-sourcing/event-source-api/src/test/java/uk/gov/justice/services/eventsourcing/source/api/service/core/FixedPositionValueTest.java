package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FixedPositionValueTest {

    @Test
    public void shouldReturnHeadPositionValue() throws Exception {
        assertThat(FixedPositionValue.HEAD, is("HEAD"));
    }

    @Test
    public void shouldReturnFirstPositionValue() throws Exception {
        assertThat(FixedPositionValue.FIRST, is("1"));
    }

}