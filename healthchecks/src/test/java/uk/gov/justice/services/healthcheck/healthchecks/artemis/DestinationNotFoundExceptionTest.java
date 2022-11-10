package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class DestinationNotFoundExceptionTest {

    @Test
    public void formattedDestinationNames_shouldReturnCommaSeparatedDestinations() {
        DestinationNotFoundException e = new DestinationNotFoundException(List.of("d-1", "d-2"), new IOException());

        assertThat(e.formattedDestinationNames(), CoreMatchers.is("d-1, d-2"));
    }

    @Test
    public void formattedDestinationNames_shouldReturnEmptyStringWhenInputListIsEmpty() {
        DestinationNotFoundException e = new DestinationNotFoundException(List.of(), new IOException());

        assertThat(e.formattedDestinationNames(), CoreMatchers.is(""));
    }

}