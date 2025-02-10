package uk.gov.justice.services.event.buffer.core.repository.streamerror;

public class StreamErrorHandlingException extends RuntimeException {

    public StreamErrorHandlingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
