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
        String fullStackTrace) {

    @Override
    public String toString() {
        return "StreamError{" +
                "id=" + id +
                ", hash='" + hash + '\'' +
                ", exceptionClassName='" + exceptionClassName + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", causeClassName=" + causeClassName +
                ", causeMessage=" + causeMessage +
                ", javaClassname='" + javaClassname + '\'' +
                ", javaMethod='" + javaMethod + '\'' +
                ", javaLineNumber=" + javaLineNumber +
                ", eventName='" + eventName + '\'' +
                ", eventId=" + eventId +
                ", streamId=" + streamId +
                ", positionInStream=" + positionInStream +
                ", dateCreated=" + dateCreated +
                ", fullStackTrace='" + fullStackTrace + '\'' +
                '}';
    }
}
