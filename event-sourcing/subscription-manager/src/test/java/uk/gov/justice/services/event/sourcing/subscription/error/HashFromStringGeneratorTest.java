package uk.gov.justice.services.event.sourcing.subscription.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HashFromStringGeneratorTest {

    @InjectMocks
    private HashFromStringGenerator hashFromStringGenerator;

    @Test
    public void shouldGenerateReadableHashStringFromExceptionClassNameCauseFailingClassMethodAndLineNumber() throws Exception {

        final String exception = "uk.gov.justice.event.SomeException";
        final String cause = NullPointerException.class.getName();
        final String className = "uk.gov.justice.event.SomeClass";
        final String methodName = "someMethod";
        final int lineNumber = 23;

        final StringBuilder stringBuilder = new StringBuilder();

        final String hash = stringBuilder
                .append(className).append("_")
                .append(methodName).append("_")
                .append(lineNumber)
                .append(exception).append("_")
                .append(cause).append("_")
                .toString();

        final String hashString = hashFromStringGenerator.createHashFrom(hash);

        assertThat(hashString, is("cd9c31dcc20f83edfb2dc35d100c7b869c9d2f9c3b404dc1443e4255"));
    }
}