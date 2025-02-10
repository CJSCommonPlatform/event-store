package uk.gov.justice.services.event.sourcing.subscription.error;

import uk.gov.justice.services.common.configuration.errors.event.EventErrorHandlingConfiguration;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

public class StreamProcessingFailureHandler {

    @Inject
    private ExceptionDetailsRetriever exceptionDetailsRetriever;

    @Inject
    private StreamErrorConverter streamErrorConverter;

    @Inject
    private StreamErrorService streamErrorService;

    @Inject
    private EventErrorHandlingConfiguration eventErrorHandlingConfiguration;

    public void onStreamProcessingFailure(final JsonEnvelope jsonEnvelope, final Throwable exception) {

        if(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()) {
            final ExceptionDetails exceptionDetails = exceptionDetailsRetriever.getExceptionDetailsFrom(exception);
            final StreamError streamError = streamErrorConverter.asStreamError(exceptionDetails, jsonEnvelope);

            streamErrorService.markStreamAsErrored(streamError);
        }
    }
}
