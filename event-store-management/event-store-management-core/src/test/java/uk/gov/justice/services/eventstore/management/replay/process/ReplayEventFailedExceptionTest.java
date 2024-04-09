package uk.gov.justice.services.eventstore.management.replay.process;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReplayEventFailedExceptionTest {

    @Test
    public void validateWithCause() {
        final Throwable cause = new Throwable("An error occurred");

        ReplayEventFailedException e = new ReplayEventFailedException("An error occurred", cause);

        assertThat(e.getMessage(), is("An error occurred"));
        assertThat(e.getCause(), is(cause));
    }

    @Test
    public void validateWithOnlyMessage() {
        ReplayEventFailedException e = new ReplayEventFailedException("An error occurred");

        assertThat(e.getMessage(), is("An error occurred"));
    }
}