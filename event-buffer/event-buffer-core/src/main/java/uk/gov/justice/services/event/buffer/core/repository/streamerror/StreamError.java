package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public record StreamError(
        UUID id,
        String hash,
        String exceptionClassName,
        String exceptionMessage,
        Optional<String> causeClassName,
        Optional<String> causeMessage,
        String javaClassname,
        String javaMethod,
        int javaLineNumber,
        String eventName,
        UUID eventId,
        UUID streamId,
        Long positionInStream,
        ZonedDateTime dateCreated,
        String fullStackTrace,
        String componentName,
        String source) {
}
