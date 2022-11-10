package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

public class DestinationNotFoundExceptionTest {

    @Test
    public void getMessage() {
        DestinationNotFoundException e = new DestinationNotFoundException("ex message", new IOException());

        assertThat(e.getMessage(), CoreMatchers.is("ex message"));
    }
}