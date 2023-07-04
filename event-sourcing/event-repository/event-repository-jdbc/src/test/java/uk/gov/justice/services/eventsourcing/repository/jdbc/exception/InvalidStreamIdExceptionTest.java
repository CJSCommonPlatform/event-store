package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

public class InvalidStreamIdExceptionTest {

    @Test
    public void shouldCreateInstanceOfInvalidStreamIdExceptionWithMessage() throws Exception {
        final InvalidStreamIdException exception = new InvalidStreamIdException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(RuntimeException.class));
    }
}