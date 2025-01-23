package uk.gov.justice.services.event.sourcing.subscription.error;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StackTraceProviderTest {

    @InjectMocks
    private StackTraceProvider stackTraceProvider;

    @Test
    public void shouldGetTheStackTraceFromAnExceptionAsString() throws Exception {

        final String stackTrace = stackTraceProvider.getStackTraceFrom(new ExceptionHashingException("Oh my"));

        assertThat(stackTrace, startsWith("uk.gov.justice.services.event.sourcing.subscription.error.ExceptionHashingException: Oh my\n" +
                "\tat uk.gov.justice.services.event.sourcing.subscription.error.StackTraceProviderTest"));
    }
}