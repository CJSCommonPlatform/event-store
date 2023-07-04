package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public class DestinationNotFoundExceptionTest {

    @Test
    public void getMessage() {
        DestinationNotFoundException e = new DestinationNotFoundException("ex message");

        assertThat(e.getMessage(), CoreMatchers.is("ex message"));
    }
}