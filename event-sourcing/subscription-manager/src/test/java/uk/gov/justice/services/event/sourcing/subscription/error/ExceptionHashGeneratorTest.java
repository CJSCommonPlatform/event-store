package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExceptionHashGeneratorTest {

    @Mock
    private HashFromStringGenerator hashFromStringGenerator;

    @InjectMocks
    private ExceptionHashGenerator exceptionHashGenerator;

    @Test
    public void shouldGenerateHasFromClassNameMethodLineNumberExceptionClassNameAndCause() throws Exception {

        final String className = "uk.gov.justice.core.SomeClass";
        final String exceptionClassName = "uk.gov.justice.core.error.SomeException";
        final String methodName = "someMethod";
        final int lineNumber = 23;
        final Optional<String> causeClassName = of("uk.gov.justice.core.error.SomeCauseException");
        final String expectedRawString = "uk.gov.justice.core.SomeClass_someMethod_23_uk.gov.justice.core.error.SomeException_uk.gov.justice.core.error.SomeCauseException";
        final String expectedHash = "576b975aff05b7f2b4a1f7b26eb47aa5";

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn(className);
        when(stackTraceElement.getMethodName()).thenReturn(methodName);
        when(stackTraceElement.getLineNumber()).thenReturn(lineNumber);
        when(hashFromStringGenerator.createHashFrom(expectedRawString)).thenReturn(expectedHash);

        assertThat(exceptionHashGenerator.createHashStringFrom(stackTraceElement, exceptionClassName, causeClassName), is(expectedHash));
    }

    @Test
    public void shouldHandleMissingCauseException() throws Exception {

        final String className = "uk.gov.justice.core.SomeClass";
        final String exceptionClassName = "uk.gov.justice.core.error.SomeException";
        final String methodName = "someMethod";
        final int lineNumber = 23;
        final Optional<String> causeClassName = empty();
        final String expectedRawString = "uk.gov.justice.core.SomeClass_someMethod_23_uk.gov.justice.core.error.SomeException";
        final String expectedHash = "576b975aff05b7f2b4a1f7b26eb47aa5";

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn(className);
        when(stackTraceElement.getMethodName()).thenReturn(methodName);
        when(stackTraceElement.getLineNumber()).thenReturn(lineNumber);
        when(hashFromStringGenerator.createHashFrom(expectedRawString)).thenReturn(expectedHash);

        assertThat(exceptionHashGenerator.createHashStringFrom(stackTraceElement, exceptionClassName, causeClassName), is(expectedHash));
    }
}