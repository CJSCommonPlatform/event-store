package uk.gov.justice.services.event.sourcing.subscription.error;

public class FrameworkClassNameFilter {

    public boolean isFrameworkOrContextClass(final StackTraceElement stackTraceElement) {

        final String className = stackTraceElement.getClassName();
        return (className.startsWith("uk.gov.justice") ||
                className.startsWith("uk.gov.moj.cpp")) &&
                !className.contains("$$");
    }
}
