package uk.gov.justice.services.event.sourcing.subscription.error;

import java.util.List;
import java.util.Optional;

public record ExceptionDetails(
        Throwable originalException,
        Optional<Throwable> cause,
        List<StackTraceElement> stackTraceElements,
        String fullStackTrace) {
}
