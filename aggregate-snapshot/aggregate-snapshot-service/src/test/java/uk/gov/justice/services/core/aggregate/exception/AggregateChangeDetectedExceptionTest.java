package uk.gov.justice.services.core.aggregate.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

public class AggregateChangeDetectedExceptionTest {

    @Test
    public void shouldCreateInstanceOfAggregateChangeDetectedExceptionWithMessage() throws Exception {
        final AggregateChangeDetectedException exception = new AggregateChangeDetectedException("Test message", 0L, null);
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(Exception.class));
    }
}