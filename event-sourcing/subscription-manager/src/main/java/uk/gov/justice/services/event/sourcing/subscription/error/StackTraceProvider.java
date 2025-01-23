package uk.gov.justice.services.event.sourcing.subscription.error;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceProvider {

    public String getStackTraceFrom(final Throwable exception) {
        final StringWriter stringWriter = new StringWriter();
        try(final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            exception.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
}
