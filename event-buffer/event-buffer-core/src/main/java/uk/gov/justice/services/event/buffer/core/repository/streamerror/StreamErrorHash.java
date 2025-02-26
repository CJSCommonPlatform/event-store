package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import java.util.Optional;

public record StreamErrorHash(
        String hash,
        String exceptionClassName,
        Optional<String> causeClassName,
        String javaClassName,
        String javaMethod,
        int javaLineNumber) {
}
