package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class InvalidSequenceIdExceptionTest {

    @Test
    public void shouldCreateInstanceOfInvalidSequenceIdExceptionWithMessage() throws Exception {
        final InvalidPositionException exception = new InvalidPositionException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(Exception.class));
    }
}